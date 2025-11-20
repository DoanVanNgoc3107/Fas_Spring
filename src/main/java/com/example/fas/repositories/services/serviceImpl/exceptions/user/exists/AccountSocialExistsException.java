package com.example.fas.repositories.services.serviceImpl.exceptions.user.exists;

public class AccountSocialExistsException extends RuntimeException {
    public AccountSocialExistsException(String message) {
        super(message);
    }
}
