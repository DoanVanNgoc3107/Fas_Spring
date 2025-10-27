package com.example.fas.exceptions.user.exists;

public class AccountSocialExistsException extends RuntimeException {
    public AccountSocialExistsException(String message) {
        super(message);
    }
}
