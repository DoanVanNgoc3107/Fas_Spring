package com.example.fas.repositories.services.serviceImpl.exceptions.user.notFound;

public class ProviderIdNotFoundException extends RuntimeException {
    public ProviderIdNotFoundException(String message) {
        super(message);
    }
}
