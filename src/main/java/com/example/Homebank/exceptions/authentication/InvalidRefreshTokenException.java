package com.example.Homebank.exceptions.authentication;

import com.example.Homebank.error.ApiErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a provided refresh token cannot be matched to a valid user session.
 */
public class InvalidRefreshTokenException extends InvalidTokenException {
    public InvalidRefreshTokenException() {
        super(
                TokenType.REFRESH,
                HttpStatus.UNAUTHORIZED,
                ApiErrorCode.TOKEN_INVALID,
                "Invalid refresh token."
        );
    }
}
