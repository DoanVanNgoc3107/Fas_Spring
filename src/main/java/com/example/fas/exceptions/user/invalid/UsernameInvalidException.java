package com.example.fas.exceptions.user.invalid;

public class UsernameInvalidException extends RuntimeException {
    public UsernameInvalidException(String message) {
        super(message);
    }
}
