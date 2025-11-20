package com.example.fas.repositories.services.serviceImpl.exceptions.user.exists;

public class EmailExistsException extends RuntimeException {
    public EmailExistsException(String message) {
        super(message);
    }
    
}
