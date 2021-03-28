package springboot.soccer.game.team.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import springboot.soccer.game.team.datatransferobject.ErrorDTO;
import springboot.soccer.game.team.exception.EntityNotFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandlingControllerAdvice {

    private final MessageSource messageSource;

    @Autowired
    public ErrorHandlingControllerAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex, Locale locale) {
        String message = messageSource.getMessage(ex.getMessage(), ex.getArgs(), locale);
        log.debug("Errors while finding entity: %s", message);

        ErrorDTO errorDTO = new ErrorDTO(Collections.singletonMap("error", message));

        return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        log.debug("Errors while beans validation: %s", ex.getMessage());

        Map<String, String> errors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(constraintViolation -> constraintViolation.getPropertyPath().toString(), ConstraintViolation::getMessage, (existing, replacement) -> existing));
        ErrorDTO errorDTO = new ErrorDTO(errors);

        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Object> onMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.debug("Errors while beans validation: %s", ex.getMessage());

          Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField,DefaultMessageSourceResolvable::getDefaultMessage, (existing, replacement) -> existing ));
        ErrorDTO errorDTO = new ErrorDTO(errors);

        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

}