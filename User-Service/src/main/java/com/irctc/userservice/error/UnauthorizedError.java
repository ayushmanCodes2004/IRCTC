package com.irctc.userservice.error;


public class UnauthorizedError extends ApiError {
    
    public UnauthorizedError(String message) {
        super(401, message);
    }
}
