package com.example.Homebank.exceptions.authentication;

import com.example.Homebank.error.ApiErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

/**
 * Base exception for token-related failures.
 */
@Getter
public abstract class TokenException extends AuthenticationException {
    private final TokenType tokenType;
    private final HttpStatus status;
    private final ApiErrorCode code;
    private final String clientMessage;

    protected TokenException(
            TokenType tokenType,
            HttpStatus status,
            ApiErrorCode code,
            String clientMessage
    ) {
        super(clientMessage);
        this.tokenType = tokenType;
        this.status = status;
        this.code = code;
        this.clientMessage = clientMessage;
    }
}
