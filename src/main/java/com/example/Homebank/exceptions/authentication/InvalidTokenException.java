package com.example.Homebank.exceptions.authentication;

import com.example.Homebank.error.ApiErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Base exception for invalid token values.
 */
public class InvalidTokenException extends TokenException {
    protected InvalidTokenException(
            TokenType tokenType,
            HttpStatus status,
            ApiErrorCode code,
            String clientMessage
    ) {
        super(tokenType, status, code, clientMessage);
    }
}
