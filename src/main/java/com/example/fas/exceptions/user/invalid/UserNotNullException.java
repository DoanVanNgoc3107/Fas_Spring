package com.example.fas.exceptions.user.invalid;

public class UserNotNullException extends RuntimeException {
    public UserNotNullException(String message) {
        super(message);
    }
}
