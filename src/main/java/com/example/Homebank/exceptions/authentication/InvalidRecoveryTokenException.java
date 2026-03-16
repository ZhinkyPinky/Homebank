package com.example.Homebank.exceptions.authentication;

import com.example.Homebank.error.ApiErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when the provided recovery token is invalid.
 */
public class InvalidRecoveryTokenException extends InvalidTokenException {
    public InvalidRecoveryTokenException() {
        super(
                TokenType.RECOVERY,
                HttpStatus.UNAUTHORIZED,
                ApiErrorCode.TOKEN_INVALID,
                "Invalid recovery token."
        );
    }
}
