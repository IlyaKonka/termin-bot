
import lombok.extern.slf4j.Slf4j;
import telegram.TelegramBot;
import utils.BotProps;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static utils.BotConstants.*;
import static utils.BotDefaults.PROPFILE;

/**
 * Read local properties and starts bot.
 */

@Slf4j
public class BotLauncher {
    public static void main(String[] args) {

        String propPath;
        try {
            propPath = args[0];
        } catch (Exception e) {
            propPath = PROPFILE;
            log.info("Expected program argument: <properies file path>. Program started without argument. " +
                    "Using standard path for properties file: [{}]", propPath);
        }

        Properties globalProps = new Properties();
        try {
            log.info("Reading properties file: [{}]", propPath);
            globalProps.load(new FileInputStream(propPath));
        } catch (IOException e) {
            log.error("Can't read properties file: [{}]", propPath);
            System.exit(-1);
        }

        BotProps botProps = null;
        try {
            botProps = BotProps.builder()
                    .botName(globalProps.getProperty(BOT_NAME))
                    .botToken(globalProps.getProperty(BOT_TOKEN))
                    .botPort(globalProps.getProperty(BOT_PORT))
                    .terminUrl(globalProps.getProperty(TERMIN_URL))
                    .users(Arrays.asList(globalProps.getProperty(USERS).split(PROP_SPLITTER)))
                    .chatIds(Arrays.stream(globalProps.getProperty(CHAT_IDS).split(PROP_SPLITTER))
                            .filter(Predicate.not(String::isEmpty))
                            .map(Long::parseLong).collect(toList()))
                    .terminFoundString(globalProps.getProperty(TERMIN_FOUND_STRING))
                    .noTerminFoundString(globalProps.getProperty(NO_TERMIN_FOUND_STRING))
                    .buttonId(globalProps.getProperty(BUTTON_ID))
                    .build();
        } catch (Exception e) {
            log.error("Cannot parse property file", e);
            System.exit(-1);
        }

        log.info("Validating bot properties");
        ValidatorFactory factory = null;
        try {
            factory = Validation.buildDefaultValidatorFactory();
            factory.getValidator().validate(botProps).forEach(BotProps::printValidationError);
        } catch (Exception e) {
            log.error("Error during bot property validation", e);
        } finally {
            assert factory != null;
            factory.close();
        }

        log.info("Starting new bot [{}]", botProps.getBotName());
        TelegramBot telegramBot = new TelegramBot(botProps, propPath);
        telegramBot.run(); //start() for creating new thread
    }
}
