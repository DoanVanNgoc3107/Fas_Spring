package com.example.fas.repositories.services.serviceImpl.exceptions.user.invalid;

public class UpdateUserNotNullException extends RuntimeException {
    public UpdateUserNotNullException(String message) {
        super(message);
    }
}
