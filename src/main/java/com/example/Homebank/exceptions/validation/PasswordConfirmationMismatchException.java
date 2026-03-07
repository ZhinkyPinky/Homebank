package com.example.Homebank.exceptions.validation;

/**
 * Thrown when a password and confirmation password do not match.
 */
public class PasswordConfirmationMismatchException extends IllegalArgumentException {
    public PasswordConfirmationMismatchException() {
        super("PASSWORD_CONFIRMATION_MISMATCH");
    }
}
