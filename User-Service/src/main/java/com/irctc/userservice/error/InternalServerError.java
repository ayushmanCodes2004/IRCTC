package com.irctc.userservice.error;


public class InternalServerError extends ApiError {
    
    public InternalServerError(String message) {
        super(500, message);
    }
}
