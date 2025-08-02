package com.app.mybank.domain.exception.security;

import com.app.mybank.domain.exception.SecurityException;

public class UnauthorizedAccessException extends SecurityException {
    public UnauthorizedAccessException() {
        super("You don't have access to this");
    }
}
