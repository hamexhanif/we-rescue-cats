package io.werescuecats.backend.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.werescuecats.backend.exception.CatNotAvailableException;
import io.werescuecats.backend.exception.ResourceNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(CatNotAvailableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleCatNotAvailable(CatNotAvailableException ex) {
        return ex.getMessage();
    }
}
