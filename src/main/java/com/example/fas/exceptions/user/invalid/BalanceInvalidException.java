package com.example.fas.exceptions.user.invalid;

public class BalanceInvalidException extends RuntimeException {
    public BalanceInvalidException(String message) {
        super(message);
    }
}
