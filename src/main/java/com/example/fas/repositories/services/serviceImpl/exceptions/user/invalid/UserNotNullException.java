package com.example.fas.repositories.services.serviceImpl.exceptions.user.invalid;

public class UserNotNullException extends RuntimeException {
    public UserNotNullException(String message) {
        super(message);
    }
}
