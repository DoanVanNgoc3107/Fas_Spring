package com.example.fas.exceptions.user.invalid;

public class IdentityCardInvalidException extends RuntimeException {
    public IdentityCardInvalidException(String message) {
        super(message);
    }
}
