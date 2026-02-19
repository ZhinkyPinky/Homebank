package com.example.Homebank.presentation.controllers;

import com.example.Homebank.exceptions.authentication.AccountNotActivatedException;
import com.example.Homebank.exceptions.authentication.ActivationTokenExpiredException;
import com.example.Homebank.presentation.dto.ErrorResponse;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ErrorResponse> handleEntityExistsException(EntityExistsException e) {
        logger.info("Handling entity exists exception: {}", e.getMessage());

        String error = "CONFLICT";
        String message = e.getMessage() != null ? e.getMessage() : "The resource already exists.";
        ErrorResponse errorResponse = new ErrorResponse(error, message);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException e) {
        logger.info("Handling entity not found exception: {}", e.getMessage());

        String error = "NOT_FOUND";
        String message = e.getMessage() != null ? e.getMessage() : "The requested resource was not found.";
        ErrorResponse errorResponse = new ErrorResponse(error, message);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(AccountNotActivatedException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotActivatedException(AccountNotActivatedException e) {
        logger.info("Handling account not activated exception: {}", e.getMessage());

        String error = "ACCOUNT_NOT_ACTIVATED";
        String message = "Your account is not activated. Please check your email for the activation link.";
        ErrorResponse errorResponse = new ErrorResponse(error, message);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(ActivationTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleActivationTokenExpiredException(ActivationTokenExpiredException e) {
        logger.info("Handling activation token expired exception: {}", e.getMessage());

        String error = "ACTIVATION_TOKEN_EXPIRED";
        String message = "Your activation token has expired. Please request a new activation email.";

        ErrorResponse errorResponse = new ErrorResponse(error, message);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException e) {
        logger.info("Handling bad credentials exception: {}", e.getMessage());

        String error = "BAD_CREDENTIALS";
        String message = "Invalid email or password.";
        ErrorResponse errorResponse = new ErrorResponse(error, message);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledException(DisabledException e) {
        logger.info("Handling disabled account exception: {}", e.getMessage());

        String error = "ACCOUNT_DISABLED";
        String message = "Your account has been disabled.";
        ErrorResponse errorResponse = new ErrorResponse(error, message);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        logger.info("Handling authentication exception: {}", e.getMessage());

        String error = "AUTHENTICATION_FAILED";
        String message = "Authentication failed.";
        ErrorResponse errorResponse = new ErrorResponse(error, message);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.info("Handling illegal argument exception: {}", e.getMessage());

        String error = "BAD_REQUEST";
        String message = e.getMessage() != null ? e.getMessage() : "Invalid request parameters.";
        ErrorResponse errorResponse = new ErrorResponse(error, message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        logger.info("Handling method argument not valid exception: {}", e.getMessage());

        String error = "VALIDATION_FAILED";
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));

        if (message.isBlank()) {
            message = "Validation failed for one or more fields.";
        }

        ErrorResponse errorResponse = new ErrorResponse(error, message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
