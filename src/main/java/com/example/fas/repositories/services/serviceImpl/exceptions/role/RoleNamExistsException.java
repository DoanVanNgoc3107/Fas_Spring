package com.example.fas.repositories.services.serviceImpl.exceptions.role;

public class RoleNamExistsException extends RuntimeException {
    public RoleNamExistsException(String message) {
        super(message);
    }
}
