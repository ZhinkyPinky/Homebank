package com.example.Homebank.exceptions.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * Thrown when the provided recovery token is invalid.
 */
public class InvalidRecoveryTokenException extends AuthenticationException {
    public InvalidRecoveryTokenException() {
        super("INVALID_RECOVERY_TOKEN");
    }
}
