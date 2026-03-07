package com.example.Homebank.presentation.controllers;

import com.example.Homebank.exceptions.authentication.AccountNotActivatedException;
import com.example.Homebank.exceptions.authentication.ActivationTokenExpiredException;
import com.example.Homebank.presentation.dto.ApiError;
import com.example.Homebank.presentation.dto.ValidationErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts application and framework exceptions into standardized {@link ApiError} responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles attempts to create resources that already exist.
     */
    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ApiError> handleEntityExistsException(EntityExistsException e, HttpServletRequest request) {
        logger.info("Handling entity exists exception: {}", e.getMessage());

        String message = e.getMessage() != null ? e.getMessage() : "The resource already exists.";
        return buildErrorResponse(HttpStatus.CONFLICT, "ENTITY_ALREADY_EXISTS", message, request, null);
    }

    /**
     * Handles missing resource errors from persistence or service layers.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleEntityNotFoundException(EntityNotFoundException e, HttpServletRequest request) {
        logger.info("Handling entity not found exception: {}", e.getMessage());

        String message = e.getMessage() != null ? e.getMessage() : "The requested resource was not found.";
        return buildErrorResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", message, request, null);
    }

    /**
     * Handles attempts to use features requiring an activated account.
     */
    @ExceptionHandler(AccountNotActivatedException.class)
    public ResponseEntity<ApiError> handleAccountNotActivatedException(AccountNotActivatedException e, HttpServletRequest request) {
        logger.info("Handling account not activated exception: {}", e.getMessage());

        String message = "Your account is not activated. Please check your email for the activation link.";
        return buildErrorResponse(HttpStatus.FORBIDDEN, "ACCOUNT_NOT_ACTIVATED", message, request, null);
    }

    /**
     * Handles activation links or tokens that are no longer valid.
     */
    @ExceptionHandler(ActivationTokenExpiredException.class)
    public ResponseEntity<ApiError> handleActivationTokenExpiredException(ActivationTokenExpiredException e, HttpServletRequest request) {
        logger.info("Handling activation token expired exception: {}", e.getMessage());

        String message = "Your activation token has expired. Please request a new activation email.";
        return buildErrorResponse(HttpStatus.FORBIDDEN, "ACTIVATION_TOKEN_EXPIRED", message, request, null);
    }

    /**
     * Handles invalid sign-in credentials.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
        logger.info("Handling bad credentials exception: {}", e.getMessage());

        String message = "Invalid email or password.";
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", message, request, null);
    }

    /**
     * Handles disabled account authentication failures.
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabledException(DisabledException e, HttpServletRequest request) {
        logger.info("Handling disabled account exception: {}", e.getMessage());

        String message = "Your account has been disabled.";
        return buildErrorResponse(HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED", message, request, null);
    }

    /**
     * Handles generic authentication failures not covered by more specific handlers.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        logger.info("Handling authentication exception: {}", e.getMessage());

        String message = "Authentication failed.";
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", message, request, null);
    }

    /**
     * Handles invalid arguments passed to controller/service methods.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        logger.info("Handling illegal argument exception: {}", e.getMessage());

        String message = e.getMessage() != null ? e.getMessage() : "Invalid request parameters.";
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message, request, null);
    }

    /**
     * Handles authorization failures for authenticated users.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        logger.info("Handling access denied exception: {}", e.getMessage());

        String message = e.getMessage() != null ? e.getMessage() : "You do not have permission to perform this action.";
        return buildErrorResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED", message, request, null);
    }

    /**
     * Handles optimistic locking conflicts caused by stale row version values.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleObjectOptimisticLockingFailureException(ObjectOptimisticLockingFailureException e, HttpServletRequest request) {
        logger.info("Handling optimistic locking failure exception: {}", e.getMessage());

        String message = "The resource was modified by another process. Please refresh and try again.";
        return buildErrorResponse(HttpStatus.CONFLICT, "ROW_VERSION_MISMATCH", message, request, null);
    }

    /**
     * Handles bean validation failures and returns per-field details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        logger.info("Handling method argument not valid exception: {}", e.getMessage());

        List<ValidationErrorDetail> details = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ValidationErrorDetail(fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList());

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                "Validation failed for one or more fields.",
                request,
                details
        );
    }

    /**
     * Fallback for unexpected uncaught exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnhandledException(Exception e, HttpServletRequest request) {
        logger.error("Handling unhandled exception: {}", e.getMessage(), e);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred.",
                request,
                null
        );
    }

    /**
     * Builds the standardized API error envelope.
     */
    private ResponseEntity<ApiError> buildErrorResponse(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            Object details
    ) {
        ApiError errorResponse = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                request.getRequestURI(),
                details
        );

        return ResponseEntity.status(status).body(errorResponse);
    }
}
