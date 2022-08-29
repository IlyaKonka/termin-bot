package telegram;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import driver.ChromeWebDriver;
import lombok.extern.slf4j.Slf4j;
import utils.BotProps;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static utils.BotConstants.*;
import static utils.BotDefaults.CHECK_TIMEOUT_MILLIS;
import static utils.BotDefaults.FILENAME;

/**
 * Creates telegram bot and initializes chrome web drive.
 */

@Slf4j
public class TelegramBot implements Runnable {

    private com.pengrad.telegrambot.TelegramBot bot;
    private BotProps botProps;
    private String propPath;
    private boolean isRunning;
    private boolean isTokenValid;


    public TelegramBot(BotProps botProps, String propPath) {
        this.botProps = botProps;
        this.propPath = propPath;
    }

    /**
     * Runs telegram bot, checks new bot events (e.g. new messages)
     * and loops termin check with chrome web driver.
     */
    @Override
    public void run() {
        isRunning = true;

        log.info("Starting telegram bot: [{}]", botProps.getBotName());
        bot = new com.pengrad.telegrambot.TelegramBot(botProps.getBotToken());
        log.info("Telegram bot [{}] was started.", botProps.getBotName());

        // Check if telegram bot works fine
        isTokenValid = (bot.execute(new GetUpdates())).isOk();

        if (isTokenValid) {
            // Register for updates
            bot.setUpdatesListener(updates -> {
                try {
                    log.info("New event(s)");
                    for (Update u : updates) {
                        processUpdate(u);
                    }
                } catch (Exception e) {
                    log.error("Problem during updating telegram bot events", e);
                }
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            });
        } else {
            log.warn("Could not connect to telegram bot. Check {}.", BOT_TOKEN);
            log.warn("Continue without telegram notifications.");
        }

        while (isRunning) {
            try {
                log.info("Start checking termin");
                checkTermin();
                log.info("End checking termin. Next try after timeout...");
                Thread.sleep(CHECK_TIMEOUT_MILLIS);
            } catch (Exception e) {
                log.error("Error during termin check", e);
            }
        }

    }

    /**
     * Sends notification and html page for all users (with chatIds)
     * that termin was found.
     *
     * @param file html page with termin
     */
    public void terminNotifyAll(byte[] file) {
        for (Long chatId : botProps.getChatIds()) {
            SendDocument doc = new SendDocument(chatId, file).fileName(FILENAME);
            bot.execute(doc);
        }

        for (Long chatId : botProps.getChatIds()) {
            SendMessage msg = new SendMessage(chatId, "###Termin was found###");
            bot.execute(msg);
        }
    }

    /**
     * Initializes chrome web browser and checks, if termin is free.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    private void checkTermin() throws IOException, InterruptedException {
        ChromeWebDriver chromeBrowser = ChromeWebDriver.builder()
                .port(botProps.getBotPort())
                .terminUrl(botProps.getTerminUrl())
                .terminFound(botProps.getTerminFoundString())
                .noTerminFound(botProps.getNoTerminFoundString())
                .buttonId(botProps.getButtonId())
                .build();
        chromeBrowser.setupBrowser();
        byte[] file = chromeBrowser.findFreeTermin();
        chromeBrowser = null;

        if (null == file) {
            log.info("Continue searching for termin...");
        } else {
            log.info("Termin was found!!!");
            if (isTokenValid)
                terminNotifyAll(file);
        }
    }

    /**
     * Processes new telegram bot event and adds new chatId, if it exists.
     *
     * @param update new telegram bot event
     * @throws IOException
     */
    private void processUpdate(Update update) throws IOException {
        String currentUser = null;
        String msgText = null;
        long chatId = 0L;

        if (null != update.message()) {
            currentUser = update.message().from().username();
            chatId = update.message().chat().id();
            msgText = update.message().text();
        } else if (null != update.callbackQuery()) {
            currentUser = update.callbackQuery().from().username();
            chatId = update.callbackQuery().message().chat().id();
            msgText = update.callbackQuery().data();
        }

        if (botProps.getUsers().contains(currentUser)) {
            log.info("Got message from: [{}], Msg: [{}], ChatID [{}] ", currentUser, msgText, chatId);

            //update chatId list
            if (!botProps.getChatIds().contains(chatId)) {
                log.info("Add new ChatID: [{}] for user: [{}] ", currentUser, chatId);
                botProps.getChatIds().add(chatId);
                Path propFilePath = Path.of(propPath);
                List<String> propList = Arrays.asList(Files.readString(propFilePath).split(System.lineSeparator()));
                propList = propList.stream()
                        .map(p -> p.startsWith(CHAT_IDS + PROP_SEPARATOR)
                                ? CHAT_IDS + PROP_SEPARATOR
                                + botProps.getChatIds().stream().map(String::valueOf).collect(Collectors.joining(PROP_SPLITTER))
                                : p)
                        .collect(toList());
                Files.write(propFilePath, propList);
            }

            SendMessage msg = new SendMessage(chatId,
                    String.format("All works fine.\nUsers: %s\nChatIds: %s",
                            botProps.getUsers().toString(), botProps.getChatIds().toString()));
            bot.execute(msg);

        } else { // unknown user
            bot.execute(new SendMessage(chatId,
                    String.format("Sorry, %s. This bot is private",
                            currentUser)));
        }
    }

}