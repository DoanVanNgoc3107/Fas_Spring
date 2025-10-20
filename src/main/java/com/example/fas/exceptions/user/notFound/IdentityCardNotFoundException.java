package com.example.fas.exceptions.user.notFound;

public class IdentityCardNotFoundException extends RuntimeException {
    public IdentityCardNotFoundException(String message) {
        super(message);
    }
}
