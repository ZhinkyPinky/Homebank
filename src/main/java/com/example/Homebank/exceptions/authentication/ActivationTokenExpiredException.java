package com.example.Homebank.exceptions.authentication;

import org.springframework.security.core.AuthenticationException;

public class ActivationTokenExpiredException extends AuthenticationException {
    public ActivationTokenExpiredException() {
        super("ACTIVATION_TOKEN_EXPIRED");
    }
}
