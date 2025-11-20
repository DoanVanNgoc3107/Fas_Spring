package com.example.fas.repositories.services.serviceImpl.exceptions.auth;

public class AccessTokenInvalidException extends RuntimeException {
    public AccessTokenInvalidException(String message) {
        super(message);
    }
}
