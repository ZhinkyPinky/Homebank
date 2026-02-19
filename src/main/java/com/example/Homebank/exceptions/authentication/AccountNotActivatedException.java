package com.example.Homebank.exceptions.authentication;

import org.springframework.security.core.AuthenticationException;

public class AccountNotActivatedException extends AuthenticationException {
    public AccountNotActivatedException() {
        super("ACCOUNT_NOT_ACTIVATED");
    }
}
