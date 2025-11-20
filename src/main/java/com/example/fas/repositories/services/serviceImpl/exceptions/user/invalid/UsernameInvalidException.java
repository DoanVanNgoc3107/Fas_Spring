package com.example.fas.repositories.services.serviceImpl.exceptions.user.invalid;

public class UsernameInvalidException extends RuntimeException {
    public UsernameInvalidException(String message) {
        super(message);
    }
}
