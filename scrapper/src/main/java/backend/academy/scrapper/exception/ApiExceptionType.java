package backend.academy.scrapper.exception;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ApiExceptionType {

    INVALID_LINK("ex.api.invalidLink", HttpStatus.BAD_REQUEST),
    NOT_SUPPORTED_SOURCE("ex.api.notSupportedSource", HttpStatus.BAD_REQUEST),
    CHAT_NOT_FOUND("ex.api.chatNotFound", HttpStatus.NOT_FOUND),
    LINK_NOT_FOUND("ex.api.linkNotFound", HttpStatus.NOT_FOUND),
    CHAT_ALREADY_EXISTS("ex.api.chatAlreadyExists", HttpStatus.CONFLICT),
    LINK_ALREADY_EXISTS("ex.api.linkAlreadyExists", HttpStatus.CONFLICT);

    private final String messageProp;
    private final HttpStatus httpStatus;

    public ApiException toException(Object... args) {
        return new ApiException(
            name(),
            messageProp,
            Arrays.stream(args)
                .map(Object::toString)
                .toArray(String[]::new),
            httpStatus
        );
    }
}
