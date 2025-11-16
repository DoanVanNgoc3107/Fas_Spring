package com.example.fas.exceptions.auth;

public class AccessTokenInvalidException extends RuntimeException {
    public AccessTokenInvalidException(String message) {
        super(message);
    }
}
