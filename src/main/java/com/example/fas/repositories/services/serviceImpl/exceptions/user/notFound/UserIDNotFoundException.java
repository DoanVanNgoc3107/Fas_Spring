package com.example.fas.repositories.services.serviceImpl.exceptions.user.notFound;

public class UserIDNotFoundException extends RuntimeException{
    public UserIDNotFoundException(String message) {
        super(message);
    }
}
