package telegram;


import browser.ChromeBrowser;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


@Slf4j
public class TelegramBot implements Runnable {

    private static final String DEFAULT_PORT = "48331";

    private static String BOT_TOKEN;
    private static String TERMIN_URL;
    private static List<String> USERS = new ArrayList<>();

    private static List<Long> CHAT_IDS = new ArrayList<>();
    private com.pengrad.telegrambot.TelegramBot bot;

    private String port;

    public TelegramBot(String port) {
        this.port = port;
    }

    @Override
    public void run() {

        log.info("Starting telegram bot");
        bot = new com.pengrad.telegrambot.TelegramBot(BOT_TOKEN);
        log.info("Telegram bot started ");


        // Register for updates
        bot.setUpdatesListener(updates -> {
            try {
                log.info("New event(s)");
                for (Update u : updates) {
                    processUpdate(u);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        for (; ; ) {

            try {
                log.info("START CHECK TERMIN");
                boolean sendInfo = checkFreeTermin();
                if (sendInfo) {
                    log.info("CONTINUE SEARCHING FOR TERMIN");
                }

                log.info("END CHECK TERMIN");
                Thread.sleep(60000);
            } catch (Exception e) {
                log.error(e.toString());
            }

        }

    }

    public void sendHtmlForAll(byte[] file) {
        for (Long chatId : CHAT_IDS) {
            SendDocument doc = new SendDocument(chatId, file);
            bot.execute(doc);
        }

        for (Long chatId : CHAT_IDS) {
            SendMessage msg = new SendMessage(chatId, "TERMIN WAS FOUND");
            bot.execute(msg);
        }
    }

    private boolean checkFreeTermin() throws IOException, InterruptedException {
        ChromeBrowser chromeBrowser = new ChromeBrowser(port, TERMIN_URL);
        chromeBrowser.setupBrowser();
        byte[] file = chromeBrowser.checkTermin();
        chromeBrowser = null;
        if (null == file) {
            return true;
        } else {
            sendHtmlForAll(file);
            return false;
        }
    }

    private void processUpdate(Update u) {
        String currUser = null;
        String msgText = null;
        long chatId = 0L;

        if (u.message() != null) {
            currUser = u.message().from().username();
            chatId = u.message().chat().id();
            msgText = u.message().text();
        } else if (u.callbackQuery() != null) {
            currUser = u.callbackQuery().from().username();
            chatId = u.callbackQuery().message().chat().id();
            msgText = u.callbackQuery().data();
        }

        if (USERS.contains(currUser)) {
            log.info("Got message from: [{}], Msg: [{}], ChatID [{}] ", currUser, msgText, chatId);
            SendMessage msg = new SendMessage(chatId, "All works fine");
            bot.execute(msg);

        } else { // UNKNOWN USER
            bot.execute(new SendMessage(chatId,
                    String.format("Sry, %s. This bot is private",
                            currUser)));
        }
    }

    public static void main(String[] args) throws IOException {

        Properties globalProps = new Properties();
        try {
            globalProps.load(new FileInputStream("global.properties"));
        } catch (IOException e) {
            log.info("Can't find global.properties file.");
            throw e;
        }

        log.info("THIS BOT IS FOR:" + globalProps.getProperty("NAME"));

        BOT_TOKEN = globalProps.getProperty("BOT_TOKEN");
        TERMIN_URL = globalProps.getProperty("TERMIN_URL");
        USERS = Arrays.asList(globalProps.getProperty("USERS").split(","));
        Arrays.asList(globalProps.getProperty("CHAT_IDS").split(","))
                .forEach(k -> CHAT_IDS.add(Long.valueOf(k)));

        String currentPort = DEFAULT_PORT;
        try {
            Integer.parseInt(args[0]);
            currentPort = (args[0]);
        } catch (Exception e) {
            log.info("Using default port [{}]", DEFAULT_PORT);
        }

        //Thread telegramTest = new Thread(new TelegramBot(currentPort));
        //telegramTest.start();

        TelegramBot telegramBot = new TelegramBot(currentPort);
        telegramBot.run();
    }

}