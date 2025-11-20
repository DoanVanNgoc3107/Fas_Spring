package com.example.fas.repositories.services.serviceImpl.exceptions.auth;

public class RefreshTokenInvalidException extends RuntimeException {
    public RefreshTokenInvalidException(String message) {
        super(message);
    }
}
