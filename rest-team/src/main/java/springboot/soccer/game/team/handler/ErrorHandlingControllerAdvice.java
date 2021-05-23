package springboot.soccer.game.team.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import springboot.soccer.game.team.datatransferobject.ErrorDTO;
import springboot.soccer.game.team.exception.BusinessException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static springboot.soccer.game.team.exception.BusinessException.ErrorCode.*;


@Slf4j
@RestControllerAdvice
public class ErrorHandlingControllerAdvice {

    private static final String ERROR = "error";
    private final MessageSource messageSource;

    @Autowired
    public ErrorHandlingControllerAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ErrorDTO> handleBusinessException(BusinessException exception, Locale locale) {
        String userMessage = messageSource.getMessage(exception.getErrorCode().name(), exception.getParams(), locale);
        int httpStatusCode = Integer.parseInt(messageSource.getMessage(exception.getErrorCode().name().concat(".code"), null, "500", locale));

        log.error(userMessage, exception);

        ErrorDTO errorDTO = new ErrorDTO(Collections.singletonMap(ERROR, userMessage));
        return new ResponseEntity<>(errorDTO, HttpStatus.resolve(httpStatusCode));
    }

    // @Validate For Validating Path Variables and Request Parameters
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorDTO> handleConstraintViolationException(ConstraintViolationException exception) {
        Map<String, String> userMessage = exception.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(constraintViolation -> constraintViolation.getPropertyPath().toString(), ConstraintViolation::getMessage, (existing, replacement) -> existing));
        log.error(userMessage.toString(), exception);

        ErrorDTO errorDTO = new ErrorDTO(userMessage);
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    // error handle for @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        Map<String, String> userMessage = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, DefaultMessageSourceResolvable::getDefaultMessage, (existing, replacement) -> existing));
        log.error(userMessage.toString(), exception);

        ErrorDTO errorDTO = new ErrorDTO(userMessage);
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorDTO> handleDataIntegrityViolationException(DataIntegrityViolationException exception, Locale locale) {
        String userMessage = messageSource.getMessage(ERROR_TO_PERSIST.name(), null, locale);
        log.error(userMessage, exception);

        ErrorDTO errorDTO = new ErrorDTO(Collections.singletonMap(ERROR, userMessage));
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorDTO> handleObjectOptimisticLockingFailureException(ObjectOptimisticLockingFailureException exception, Locale locale) {
        String userMessage = messageSource.getMessage(ERROR_TO_PERSIST_OPTIMISTIC_LOCK.name(), new Object[]{exception.getIdentifier()}, locale);
        log.error(userMessage, exception);

        ErrorDTO errorDTO = new ErrorDTO(Collections.singletonMap(ERROR, userMessage));
        return new ResponseEntity<>(errorDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleAnyOtherException(Exception exception, Locale locale) {
        String userMessage = messageSource.getMessage(GENERAL.name(), null, locale);
        log.error(userMessage, exception);

        ErrorDTO errorDTO = new ErrorDTO(Collections.singletonMap(ERROR, userMessage));
        return new ResponseEntity<>(errorDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
