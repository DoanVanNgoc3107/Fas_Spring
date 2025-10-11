package com.example.fas.exceptions.user.exists;

public class CitizenIdExistsException extends RuntimeException {
    public CitizenIdExistsException(String message) {
        super(message);
    }
}
