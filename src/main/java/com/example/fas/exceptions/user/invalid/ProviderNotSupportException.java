package com.example.fas.exceptions.user.invalid;

public class ProviderNotSupportException extends RuntimeException {
    public ProviderNotSupportException(String message) {
        super(message);
    }
}
