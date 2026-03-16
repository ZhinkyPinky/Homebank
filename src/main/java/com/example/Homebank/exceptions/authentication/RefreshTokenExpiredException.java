package com.example.Homebank.exceptions.authentication;

import com.example.Homebank.error.ApiErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a provided refresh token has expired.
 */
public class RefreshTokenExpiredException extends ExpiredTokenException {
    public RefreshTokenExpiredException() {
        super(
                TokenType.REFRESH,
                HttpStatus.UNAUTHORIZED,
                ApiErrorCode.TOKEN_EXPIRED,
                "Refresh token has expired."
        );
    }
}
