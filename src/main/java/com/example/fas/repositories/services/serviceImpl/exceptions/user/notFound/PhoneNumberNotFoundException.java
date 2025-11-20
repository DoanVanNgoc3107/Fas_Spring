package com.example.fas.repositories.services.serviceImpl.exceptions.user.notFound;

public class PhoneNumberNotFoundException extends RuntimeException {
    public PhoneNumberNotFoundException(String message) {
        super(message);
    }
}
