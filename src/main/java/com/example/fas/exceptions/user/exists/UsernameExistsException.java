package com.example.fas.exceptions.user.exists;

public class UsernameExistsException extends RuntimeException {
    public UsernameExistsException(String message) {
        super(message);
    }
    
}
