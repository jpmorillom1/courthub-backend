package com.courthub.common.exception;

public class NotFoundException extends BusinessException {
    
    public NotFoundException(String message) {
        super(message);
    }
    
    public NotFoundException(String resource, Object identifier) {
        super(String.format("%s with identifier %s not found", resource, identifier));
    }
}

