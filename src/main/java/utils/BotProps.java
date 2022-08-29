package utils;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.constraints.Size;
import java.util.List;

@Builder
@Getter
@Slf4j
public class BotProps {

    @NotEmpty(message = "[BOT_NAME] may not be empty")
    String botName;

    @NotEmpty(message = "[BOT_PORT] may not be empty")
    String botPort;

    @NotEmpty(message = "[BOT_TOKEN] may not be empty")
    String botToken;

    @Size(min = 1, message = "[USERS] may not be empty")
    List<String> users;

    @NotEmpty(message = "[TERMIN_URL] may not be empty")
    String terminUrl;

    @NotEmpty(message = "[TERMIN_FOUND_STRING] may not be empty")
    String terminFoundString;

    @NotEmpty(message = "[NO_TERMIN_FOUND_STRING] may not be empty")
    String noTerminFoundString;

    @NotEmpty(message = "[BUTTON_ID] may not be empty")
    String buttonId;

    List<Long> chatIds;

    public static void printValidationError(ConstraintViolation<BotProps> violation) {
        log.error("property: {}, msg: {}", violation.getPropertyPath(), violation.getMessage());
        throw new ValidationException();
    }
}
