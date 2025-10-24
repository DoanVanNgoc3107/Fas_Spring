package com.example.fas.exceptions.user.exists;

public class EmailExistsException extends RuntimeException {
    public EmailExistsException(String message) {
        super(message);
    }
    
}
