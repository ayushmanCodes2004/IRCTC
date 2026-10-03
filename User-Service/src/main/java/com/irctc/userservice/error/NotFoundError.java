package com.irctc.userservice.error;


public class NotFoundError extends ApiError {
    
    public NotFoundError(String message) {
        super(404, message);
    }
}
