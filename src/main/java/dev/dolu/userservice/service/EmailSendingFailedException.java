package dev.dolu.userservice.service;

public class EmailSendingFailedException extends RuntimeException {

    public EmailSendingFailedException(String message) {
        super(message);
    }

    public EmailSendingFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}