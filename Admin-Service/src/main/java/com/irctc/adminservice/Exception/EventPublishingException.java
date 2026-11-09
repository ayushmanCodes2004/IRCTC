package com.irctc.adminservice.Exception;

public class EventPublishingException extends RuntimeException {

    public EventPublishingException(String message, Throwable cause) {
        super(message, cause);
    }
}