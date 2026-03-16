package com.example.Homebank.exceptions.authentication;

import com.example.Homebank.error.ApiErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Base exception for expired tokens.
 */
public class ExpiredTokenException extends TokenException {
    protected ExpiredTokenException(
            TokenType tokenType,
            HttpStatus status,
            ApiErrorCode code,
            String clientMessage
    ) {
        super(tokenType, status, code, clientMessage);
    }
}
