package com.example.Homebank.exceptions.authentication;

import com.example.Homebank.error.ApiErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when an account activation token is invalid.
 */
public class InvalidActivationTokenException extends InvalidTokenException {
    public InvalidActivationTokenException() {
        super(
                TokenType.ACTIVATION,
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.TOKEN_INVALID,
                "Invalid activation token."
        );
    }
}
