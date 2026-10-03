package com.irctc.userservice.error;

import org.springframework.http.HttpStatus;

public class ConflictError extends ApiError {
    public ConflictError(String message) {
        super(HttpStatus.CONFLICT.value(), message);
    }
}
