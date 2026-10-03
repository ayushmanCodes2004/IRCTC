package com.irctc.userservice.error;


public class ForbiddenError extends ApiError {
    
    public ForbiddenError(String message) {
        super(403, message);
    }
}
