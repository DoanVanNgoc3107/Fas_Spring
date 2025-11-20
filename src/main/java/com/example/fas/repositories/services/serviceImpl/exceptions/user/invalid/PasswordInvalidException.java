package com.example.fas.repositories.services.serviceImpl.exceptions.user.invalid;

public class PasswordInvalidException extends RuntimeException {
    public PasswordInvalidException(String message) {
        super(message);
    }
}
