package com.irctc.userservice.error;

import lombok.Getter;

@Getter
public class ApiError extends RuntimeException {
    
    private final int statusCode;
    private final String message;

    public ApiError(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.message = message;
    }
}
