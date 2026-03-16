package com.example.Homebank.exceptions.authentication;

import com.example.Homebank.error.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class ActivationTokenExpiredException extends ExpiredTokenException {
    public ActivationTokenExpiredException() {
        super(
                TokenType.ACTIVATION,
                HttpStatus.FORBIDDEN,
                ApiErrorCode.TOKEN_EXPIRED,
                "Your activation token has expired. Please request a new activation email."
        );
    }
}
