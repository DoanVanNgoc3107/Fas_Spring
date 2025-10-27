package com.example.fas.exceptions.user.invalid;

public class UpdateUserNotNullException extends RuntimeException {
    public UpdateUserNotNullException(String message) {
        super(message);
    }
}
