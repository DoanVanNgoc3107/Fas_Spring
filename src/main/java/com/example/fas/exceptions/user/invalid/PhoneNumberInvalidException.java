package com.example.fas.exceptions.user.invalid;

public class PhoneNumberInvalidException extends RuntimeException {
    public PhoneNumberInvalidException(String message) {
        super(message);
    }
}
