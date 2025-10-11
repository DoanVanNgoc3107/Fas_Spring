package com.example.fas.exceptions.user.invalid;

public class CitizenIdInvalidException extends RuntimeException{
    public CitizenIdInvalidException(String message) {
        super(message);
    }
}
