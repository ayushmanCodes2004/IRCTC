package com.irctc.userservice.error;

public class TooManyRequestsError extends ApiError {

    public TooManyRequestsError(String message) {
        super(429, message);
    }
}
