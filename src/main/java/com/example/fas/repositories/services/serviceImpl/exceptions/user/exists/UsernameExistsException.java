package com.example.fas.repositories.services.serviceImpl.exceptions.user.exists;

public class UsernameExistsException extends RuntimeException {
    public UsernameExistsException(String message) {
        super(message);
    }
    
}
