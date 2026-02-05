package com.courthub.common.exception;

public class ForbiddenException extends BusinessException {
    
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException() {
        super("Access forbidden");
    }
}

