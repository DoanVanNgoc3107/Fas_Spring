package com.example.fas.exceptions.user.notFound;

public class EmailNotFoundException extends RuntimeException {
    public EmailNotFoundException(String message) {
        super(message);
    }
    
}
