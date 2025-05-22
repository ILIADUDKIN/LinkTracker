package backend.academy.bot.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class ApiError extends RuntimeException {
    private final String description;
    private final String code;
    private final String exceptionName;
    private final String exceptionMessage;
    private final List<String> stacktrace;

    public ApiError(String description, String code, String exceptionName,
                    String exceptionMessage, List<String> stacktrace) {
        this.description = description;
        this.code = code;
        this.exceptionName = exceptionName;
        this.exceptionMessage = exceptionMessage;
        this.stacktrace = stacktrace;
    }

}
