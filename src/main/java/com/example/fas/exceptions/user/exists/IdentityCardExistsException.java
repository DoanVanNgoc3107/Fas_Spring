package com.example.fas.exceptions.user.exists;

public class IdentityCardExistsException extends RuntimeException {
    public IdentityCardExistsException(String message) {
        super(message);
    }
    
}
