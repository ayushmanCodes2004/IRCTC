package com.irctc.userservice.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class SessionStoreUnavailableException extends RuntimeException {

    public SessionStoreUnavailableException(String message) {
        super(message);
    }
}
