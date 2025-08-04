package com.app.mybank.domain.exception.user;

import com.app.mybank.domain.exception.UserException;

public class UserNotFoundException extends UserException {
    public UserNotFoundException() {
        super("User not found");
    }
}
