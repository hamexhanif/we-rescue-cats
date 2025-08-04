package io.werescuecats.backend.exception;

public class CatNotAvailableException extends RuntimeException {
    public CatNotAvailableException(String message) {
        super(message);
    }
}
