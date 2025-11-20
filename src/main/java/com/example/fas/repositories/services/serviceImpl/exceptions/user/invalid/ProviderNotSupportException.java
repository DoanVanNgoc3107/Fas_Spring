package com.example.fas.repositories.services.serviceImpl.exceptions.user.invalid;

public class ProviderNotSupportException extends RuntimeException {
    public ProviderNotSupportException(String message) {
        super(message);
    }
}
