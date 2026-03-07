package com.example.Homebank.exceptions.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * Thrown when a provided refresh token cannot be matched to a valid user session.
 */
public class InvalidRefreshTokenException extends AuthenticationException {
    public InvalidRefreshTokenException() {
        super("INVALID_REFRESH_TOKEN");
    }
}
