package com.example.Homebank.presentation.controllers;

import com.example.Homebank.exceptions.authentication.AccountNotActivatedException;
import com.example.Homebank.exceptions.authentication.ActivationTokenExpiredException;
import com.example.Homebank.exceptions.authentication.InvalidActivationTokenException;
import com.example.Homebank.exceptions.authentication.InvalidRecoveryTokenException;
import com.example.Homebank.exceptions.authentication.InvalidRefreshTokenException;
import com.example.Homebank.exceptions.authentication.RefreshTokenExpiredException;
import com.example.Homebank.exceptions.validation.PasswordConfirmationMismatchException;
import com.example.Homebank.presentation.dto.ApiError;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTests {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleAccessDeniedExceptionShouldReturnForbiddenApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/customers/123");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleAccessDeniedException(
                new AccessDeniedException("No access."),
                request
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACCESS_DENIED", response.getBody().code());
        assertEquals("/customers/123", response.getBody().path());
        assertEquals(403, response.getBody().status());
    }

    @Test
    void handleUnhandledExceptionShouldReturnInternalServerErrorApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/customers");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleUnhandledException(
                new RuntimeException("Unexpected failure"),
                request
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().code());
        assertEquals("/customers", response.getBody().path());
        assertEquals(500, response.getBody().status());
    }

    @Test
    void handleInvalidRefreshTokenExceptionShouldReturnUnauthorizedApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/refresh");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleInvalidRefreshTokenException(
                new InvalidRefreshTokenException(),
                request
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_REFRESH_TOKEN", response.getBody().code());
        assertEquals("/auth/refresh", response.getBody().path());
        assertEquals(401, response.getBody().status());
    }

    @Test
    void handlePasswordConfirmationMismatchExceptionShouldReturnBadRequestApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/users/changePassword");

        ResponseEntity<ApiError> response = globalExceptionHandler.handlePasswordConfirmationMismatchException(
                new PasswordConfirmationMismatchException(),
                request
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PASSWORD_CONFIRMATION_MISMATCH", response.getBody().code());
        assertEquals("/users/changePassword", response.getBody().path());
        assertEquals(400, response.getBody().status());
    }

    @Test
    void handleRefreshTokenExpiredExceptionShouldReturnUnauthorizedApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/refresh");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleRefreshTokenExpiredException(
                new RefreshTokenExpiredException(),
                request
        );

        assertApiError(response, HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED", "/auth/refresh");
    }

    @Test
    void handleInvalidRecoveryTokenExceptionShouldReturnUnauthorizedApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/account-recovery/set-new-password");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleInvalidRecoveryTokenException(
                new InvalidRecoveryTokenException(),
                request
        );

        assertApiError(response, HttpStatus.UNAUTHORIZED, "INVALID_RECOVERY_TOKEN", "/account-recovery/set-new-password");
    }

    @Test
    void handleInvalidActivationTokenExceptionShouldReturnBadRequestApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/activate");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleInvalidActivationTokenException(
                new InvalidActivationTokenException(),
                request
        );

        assertApiError(response, HttpStatus.BAD_REQUEST, "INVALID_ACTIVATION_TOKEN", "/auth/activate");
    }

    @Test
    void handleActivationTokenExpiredExceptionShouldReturnForbiddenApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/activate");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleActivationTokenExpiredException(
                new ActivationTokenExpiredException(),
                request
        );

        assertApiError(response, HttpStatus.FORBIDDEN, "ACTIVATION_TOKEN_EXPIRED", "/auth/activate");
    }

    @Test
    void handleAccountNotActivatedExceptionShouldReturnForbiddenApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/refresh");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleAccountNotActivatedException(
                new AccountNotActivatedException(),
                request
        );

        assertApiError(response, HttpStatus.FORBIDDEN, "ACCOUNT_NOT_ACTIVATED", "/auth/refresh");
    }

    @Test
    void handleEntityNotFoundExceptionShouldReturnNotFoundApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/customers/999");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleEntityNotFoundException(
                new EntityNotFoundException("Missing"),
                request
        );

        assertApiError(response, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "/customers/999");
    }

    @Test
    void handleEntityExistsExceptionShouldReturnConflictApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/register");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleEntityExistsException(
                new EntityExistsException("Exists"),
                request
        );

        assertApiError(response, HttpStatus.CONFLICT, "ENTITY_ALREADY_EXISTS", "/auth/register");
    }

    @Test
    void handleBadCredentialsExceptionShouldReturnUnauthorizedApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleBadCredentialsException(
                new BadCredentialsException("Bad credentials"),
                request
        );

        assertApiError(response, HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", "/auth/login");
    }

    @Test
    void handleDisabledExceptionShouldReturnForbiddenApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleDisabledException(
                new DisabledException("Disabled"),
                request
        );

        assertApiError(response, HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED", "/auth/login");
    }

    @Test
    void handleAuthenticationExceptionShouldReturnUnauthorizedApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleAuthenticationException(
                new AuthenticationServiceException("Auth failed"),
                request
        );

        assertApiError(response, HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", "/auth/login");
    }

    @Test
    void handleIllegalArgumentExceptionShouldReturnBadRequestApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/users/changePassword");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleIllegalArgumentException(
                new IllegalArgumentException("Bad input"),
                request
        );

        assertApiError(response, HttpStatus.BAD_REQUEST, "BAD_REQUEST", "/users/changePassword");
    }

    @Test
    void handleObjectOptimisticLockingFailureExceptionShouldReturnConflictApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/customers/10");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleObjectOptimisticLockingFailureException(
                new ObjectOptimisticLockingFailureException(Object.class, 10),
                request
        );

        assertApiError(response, HttpStatus.CONFLICT, "ROW_VERSION_MISMATCH", "/customers/10");
    }

    private void assertApiError(ResponseEntity<ApiError> response, HttpStatus expectedStatus, String expectedCode, String expectedPath) {
        assertEquals(expectedStatus, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedCode, response.getBody().code());
        assertEquals(expectedPath, response.getBody().path());
        assertEquals(expectedStatus.value(), response.getBody().status());
    }
}
