package com.example.fas.repositories.services.serviceImpl.exceptions.user.notFound;

public class UsernameNotFoundException extends RuntimeException {
    public UsernameNotFoundException(String message) {
        super(message);
    }
}
