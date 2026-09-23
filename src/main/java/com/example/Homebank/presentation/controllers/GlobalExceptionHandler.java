package com.example.Homebank.presentation.controllers;

import com.example.Homebank.error.ApiErrorCode;
import com.example.Homebank.exceptions.authentication.AccountNotActivatedException;
import com.example.Homebank.exceptions.authentication.TokenException;
import com.example.Homebank.exceptions.notfound.ResourceNotFoundException;
import com.example.Homebank.exceptions.authorization.ResourceAccessDeniedException;
import com.example.Homebank.exceptions.validation.PasswordConfirmationMismatchException;
import com.example.Homebank.presentation.dto.error.ApiError;
import com.example.Homebank.presentation.dto.error.ResourceAction;
import com.example.Homebank.presentation.dto.error.ResourceAccessDeniedDetail;
import com.example.Homebank.presentation.dto.error.ResourceNotFoundDetail;
import com.example.Homebank.presentation.dto.error.ResourceType;
import com.example.Homebank.presentation.dto.error.TokenErrorDetail;
import com.example.Homebank.presentation.dto.error.ValidationErrorCode;
import com.example.Homebank.presentation.dto.error.ValidationErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
        return buildErrorResponse(HttpStatus.CONFLICT, ApiErrorCode.ENTITY_ALREADY_EXISTS, message, request, null);
    }

    /**
     * Handles missing resource errors from persistence or service layers.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleEntityNotFoundException(EntityNotFoundException e, HttpServletRequest request) {
        logger.info("Handling entity not found exception: {}", e.getMessage());

        Object details = null;
        if (e instanceof ResourceNotFoundException resourceNotFoundException) {
            details = new ResourceNotFoundDetail(
                    toResourceType(resourceNotFoundException.getResourceType()),
                    resourceNotFoundException.getResourceId(),
                    resourceNotFoundException.getMetadata().isEmpty() ? null : resourceNotFoundException.getMetadata()
            );
        }

        String message = e.getMessage() != null ? e.getMessage() : "The requested resource was not found.";
        return buildErrorResponse(HttpStatus.NOT_FOUND, ApiErrorCode.RESOURCE_NOT_FOUND, message, request, details);
    }

    /**
     * Handles attempts to use features requiring an activated account.
     */
    @ExceptionHandler(AccountNotActivatedException.class)
    public ResponseEntity<ApiError> handleAccountNotActivatedException(AccountNotActivatedException e, HttpServletRequest request) {
        logger.info("Handling account not activated exception: {}", e.getMessage());

        String message = "Your account is not activated. Please check your email for the activation link.";
        return buildErrorResponse(HttpStatus.FORBIDDEN, ApiErrorCode.ACCOUNT_NOT_ACTIVATED, message, request, null);
    }

    /**
     * Handles token-related failures and includes token-type details for client parsing.
     */
    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ApiError> handleTokenException(TokenException e, HttpServletRequest request) {
        logger.info("Handling token exception: {} ({})", e.getCode(), e.getTokenType());

        TokenErrorDetail details = new TokenErrorDetail(e.getTokenType());
        return buildErrorResponse(e.getStatus(), e.getCode(), e.getClientMessage(), request, details);
    }

    /**
     * Handles password confirmation mismatch validation errors.
     */
    @ExceptionHandler(PasswordConfirmationMismatchException.class)
    public ResponseEntity<ApiError> handlePasswordConfirmationMismatchException(PasswordConfirmationMismatchException e, HttpServletRequest request) {
        logger.info("Handling password confirmation mismatch exception: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ApiErrorCode.PASSWORD_CONFIRMATION_MISMATCH, "Passwords do not match.", request, null);
    }

    /**
     * Handles invalid sign-in credentials.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
        logger.info("Handling bad credentials exception: {}", e.getMessage());

        String message = "Invalid email or password.";
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ApiErrorCode.BAD_CREDENTIALS, message, request, null);
    }

    /**
     * Handles disabled account authentication failures.
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabledException(DisabledException e, HttpServletRequest request) {
        logger.info("Handling disabled account exception: {}", e.getMessage());

        String message = "Your account has been disabled.";
        return buildErrorResponse(HttpStatus.FORBIDDEN, ApiErrorCode.ACCOUNT_DISABLED, message, request, null);
    }

    /**
     * Handles generic authentication failures not covered by more specific handlers.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        logger.info("Handling authentication exception: {}", e.getMessage());

        String message = "Authentication failed.";
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ApiErrorCode.AUTHENTICATION_FAILED, message, request, null);
    }

    /**
     * Handles invalid arguments passed to controller/service methods.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        logger.info("Handling illegal argument exception: {}", e.getMessage());

        String message = e.getMessage() != null ? e.getMessage() : "Invalid request parameters.";
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, message, request, null);
    }

    /**
     * Handles request parameters that cannot be converted to their required type.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        logger.info("Handling type mismatch for request parameter: {}", e.getName());

        String message = "Invalid value for parameter '" + e.getName() + "'.";
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, message, request, null);
    }

    /**
     * Handles missing required request parameters.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        String message = "Required parameter '" + e.getParameterName() + "' is missing.";
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, message, request, null);
    }

    /**
     * Handles malformed JSON and request body values that cannot be deserialized.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        logger.debug("Handling unreadable request body", e);

        String message = "The request body is missing or contains malformed JSON or invalid field values.";
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, message, request, null);
    }

    /**
     * Handles unsupported HTTP methods and preserves the supported methods in the Allow header.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        logger.info("Handling unsupported HTTP method: {}", e.getMethod());

        ResponseEntity<ApiError> response = buildErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                ApiErrorCode.METHOD_NOT_ALLOWED,
                "The HTTP method is not supported for this endpoint.",
                request,
                null
        );
        return ResponseEntity.status(response.getStatusCode())
                .headers(e.getHeaders())
                .body(response.getBody());
    }

    /**
     * Handles authorization failures for authenticated users.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        logger.info("Handling access denied exception: {}", e.getMessage());

        Object details = null;
        ApiErrorCode code = ApiErrorCode.ACCESS_DENIED;
        String message = e.getMessage() != null ? e.getMessage() : "You do not have permission to perform this action.";
        if (e instanceof ResourceAccessDeniedException resourceException) {
            code = ApiErrorCode.RESOURCE_ACCESS_DENIED;
            details = new ResourceAccessDeniedDetail(
                    toResourceType(resourceException.getResourceType()),
                    resourceException.getResourceId(),
                    toResourceAction(resourceException.getAction()),
                    resourceException.getMetadata().isEmpty() ? null : resourceException.getMetadata()
            );
        }

        return buildErrorResponse(HttpStatus.FORBIDDEN, code, message, request, details);
    }

    /**
     * Handles optimistic locking conflicts caused by stale row version values.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleObjectOptimisticLockingFailureException(ObjectOptimisticLockingFailureException e, HttpServletRequest request) {
        logger.info("Handling optimistic locking failure exception: {}", e.getMessage());

        String message = "The resource was modified by another process. Please refresh and try again.";
        return buildErrorResponse(HttpStatus.CONFLICT, ApiErrorCode.ROW_VERSION_MISMATCH, message, request, null);
    }

    /**
     * Handles bean validation failures and returns per-field details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        logger.info("Handling method argument not valid exception: {}", e.getMessage());

        List<ValidationErrorDetail> details = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ValidationErrorDetail(
                        toValidationCode(fieldError.getCode()),
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.VALIDATION_FAILED,
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
                ApiErrorCode.INTERNAL_SERVER_ERROR,
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
            ApiErrorCode code,
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

    /**
     * Converts a validation constraint code to a {@link ValidationErrorCode} enum.
     * If the code is unrecognized, returns {@link ValidationErrorCode#UNKNOWN}.
     */
    private ValidationErrorCode toValidationCode(String constraintCode) {
        if (constraintCode == null || constraintCode.isBlank()) {
            return ValidationErrorCode.VALIDATION_ERROR;
        }

        String normalizedCode = constraintCode
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toUpperCase();

        try {
            return ValidationErrorCode.valueOf(normalizedCode);
        } catch (IllegalArgumentException ignored) {
            return ValidationErrorCode.UNKNOWN;
        }
    }

    /**
     * Converts a resource type string to a {@link ResourceType} enum.
     * If the type is unrecognized, returns {@link ResourceType#UNKNOWN}.
     */
    private ResourceType toResourceType(String resourceType) {
        if (resourceType == null || resourceType.isBlank()) {
            return ResourceType.UNKNOWN;
        }

        try {
            return ResourceType.valueOf(resourceType);
        } catch (IllegalArgumentException ignored) {
            return ResourceType.UNKNOWN;
        }
    }

    /**
     * Converts a resource action string to a {@link ResourceAction} enum.
     * If the action is unrecognized, returns {@link ResourceAction#UNKNOWN}.
     */
    private ResourceAction toResourceAction(String action) {
        if (action == null || action.isBlank()) {
            return ResourceAction.UNKNOWN;
        }

        try {
            return ResourceAction.valueOf(action.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return ResourceAction.UNKNOWN;
        }
    }
}
