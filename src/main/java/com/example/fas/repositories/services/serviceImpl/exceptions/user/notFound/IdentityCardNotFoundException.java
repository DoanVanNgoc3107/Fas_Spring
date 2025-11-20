package com.example.fas.repositories.services.serviceImpl.exceptions.user.notFound;

public class IdentityCardNotFoundException extends RuntimeException {
    public IdentityCardNotFoundException(String message) {
        super(message);
    }
}
