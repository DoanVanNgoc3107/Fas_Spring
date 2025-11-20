package com.example.fas.repositories.services.serviceImpl.exceptions.user.exists;

public class PhoneNumberExistsException extends RuntimeException {
    public PhoneNumberExistsException(String message) {
        super(message);
    }
}
