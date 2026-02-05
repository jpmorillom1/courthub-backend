package com.courthub.booking.domain;

import com.courthub.common.exception.BusinessException;

public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(message);
    }
}
