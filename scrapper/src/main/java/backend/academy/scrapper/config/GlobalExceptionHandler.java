package backend.academy.scrapper.config;

import backend.academy.scrapper.exception.ApiErrorResponse;
import backend.academy.scrapper.exception.ApiException;
import backend.academy.scrapper.utils.LocaleMessages;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String VALIDATION_FAILURE_CODE = "VALIDATION_FAILURE";
    private final LocaleMessages messages;

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handleApiErrorException(ApiException ex) {
        ApiErrorResponse response = new ApiErrorResponse(
            getResponseDescription(ex.status()),
            ex.code(),
            ex.getClass().getSimpleName(),
            messages.getMessageFromProps(ex.messageProp(), ex.messageArgs()),
            Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .toList()
        );
        return ResponseEntity.status(ex.status()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
        return new ApiErrorResponse(
            getResponseDescription(HttpStatus.BAD_REQUEST),
            VALIDATION_FAILURE_CODE,
            ex.getClass().getSimpleName(),
            fieldsValidationMessage(ex.getBindingResult()),
            Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .toList()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleConstraintViolationException(ConstraintViolationException ex) {
        return new ApiErrorResponse(
            getResponseDescription(HttpStatus.BAD_REQUEST),
            VALIDATION_FAILURE_CODE,
            ex.getClass().getSimpleName(),
            constraintViolationMessage(ex.getConstraintViolations()),
            Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .toList()
        );
    }

    private String getResponseDescription(HttpStatus status) {
        return String.format("%s %s", status.value(), status.name());
    }

    private String fieldsValidationMessage(Errors errors) {
        return errors.getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
    }

    private String constraintViolationMessage(Set<ConstraintViolation<?>> errors) {
        return errors.stream()
            .map(error -> error.getPropertyPath() + ": " + error.getMessage())
            .collect(Collectors.joining("; "));
    }
}
