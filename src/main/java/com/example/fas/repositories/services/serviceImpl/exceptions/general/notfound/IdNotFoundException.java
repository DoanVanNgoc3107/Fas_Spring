package com.example.fas.repositories.services.serviceImpl.exceptions.general.notfound;

public class IdNotFoundException extends RuntimeException {
    public IdNotFoundException(String message) {
        super(message);
    }
}
