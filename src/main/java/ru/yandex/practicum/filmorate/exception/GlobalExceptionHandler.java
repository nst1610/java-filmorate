package ru.yandex.practicum.filmorate.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        e.getBindingResult().getGlobalErrors()
            .forEach(error -> errors.put(error.getObjectName(), error.getDefaultMessage()));
        log.warn("Validation failed: {}", errors);
        return errors;
    }

    @ExceptionHandler({InvalidFilmDataException.class, InvalidUserDataException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidDataException(RuntimeException e) {
        log.warn("Add/update operation failed: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundException(NoSuchElementException e) {
        log.warn("Entity not found: {}", e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleInternalServerError(Exception e) {
        log.error("Unexpected server error", e);
        return Map.of("error", "Internal server error.");
    }
}
