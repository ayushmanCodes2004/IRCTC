package com.irctc.userservice.error;


public class BadRequestError extends ApiError {
    
    public BadRequestError(String message) {
        super(400, message);
    }
}
