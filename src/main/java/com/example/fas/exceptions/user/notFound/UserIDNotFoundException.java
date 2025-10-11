package com.example.fas.exceptions.user.notFound;

public class UserIDNotFoundException extends RuntimeException{
    public UserIDNotFoundException(String message) {
        super(message);
    }
}
