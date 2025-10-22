package com.example.fas.exceptions.auth;

import org.springframework.security.core.AuthenticationException;

public class LoginFailedException extends AuthenticationException {
    public LoginFailedException() {
        super("Login failed: ");
    }
}
