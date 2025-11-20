package com.example.fas.repositories.services.serviceImpl.exceptions.user.invalid;

public class EmailInvalidException extends RuntimeException {
    public EmailInvalidException(String message) {
        super(message);
    }
}
