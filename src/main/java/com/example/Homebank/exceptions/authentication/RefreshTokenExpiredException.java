package com.example.Homebank.exceptions.authentication;

import org.springframework.security.core.AuthenticationException;

/**
 * Thrown when a provided refresh token has expired.
 */
public class RefreshTokenExpiredException extends AuthenticationException {
    public RefreshTokenExpiredException() {
        super("REFRESH_TOKEN_EXPIRED");
    }
}
