package com.example.Homebank.exceptions.authentication;

/**
 * Thrown when an account activation token is invalid.
 */
public class InvalidActivationTokenException extends IllegalArgumentException {
    public InvalidActivationTokenException() {
        super("INVALID_ACTIVATION_TOKEN");
    }
}
