package com.example.Homebank.presentation.controllers;

import com.example.Homebank.exceptions.authentication.AccountNotActivatedException;
import com.example.Homebank.exceptions.authentication.ActivationTokenExpiredException;
import com.example.Homebank.exceptions.authentication.InvalidActivationTokenException;
import com.example.Homebank.exceptions.authentication.InvalidRecoveryTokenException;
import com.example.Homebank.exceptions.authentication.InvalidRefreshTokenException;
import com.example.Homebank.exceptions.authentication.RefreshTokenExpiredException;
import com.example.Homebank.exceptions.authorization.ResourceAccessDeniedException;
import com.example.Homebank.exceptions.validation.PasswordConfirmationMismatchException;
import com.example.Homebank.presentation.dto.ApiError;
import com.example.Homebank.presentation.dto.ResourceAccessDeniedDetail;
import com.example.Homebank.presentation.dto.ValidationErrorDetail;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.MethodParameter;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

        assertApiError(response, HttpStatus.FORBIDDEN, "ACCESS_DENIED", "/customers/123");
    }

    @Test
    void handleResourceAccessDeniedExceptionShouldIncludeResourceDetails() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/customers/1/transactionHeads/2");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleAccessDeniedException(
                new ResourceAccessDeniedException(
                        "TRANSACTION_HEAD",
                        2,
                        "read",
                        "Denied",
                        Map.of("customerId", 1)
                ),
                request
        );

        assertApiError(response, HttpStatus.FORBIDDEN, "RESOURCE_ACCESS_DENIED", "/customers/1/transactionHeads/2");
        assertNotNull(response.getBody());
        assertInstanceOf(ResourceAccessDeniedDetail.class, response.getBody().details());
        ResourceAccessDeniedDetail details = (ResourceAccessDeniedDetail) response.getBody().details();
        assertEquals("TRANSACTION_HEAD", details.resourceType());
        assertEquals(2, details.resourceId());
        assertEquals("read", details.action());
        assertEquals(Map.of("customerId", 1), details.metadata());
    }

    @Test
    void handleUnhandledExceptionShouldReturnInternalServerErrorApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/customers");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleUnhandledException(
                new RuntimeException("Unexpected failure"),
                request
        );

        assertApiError(response, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "/customers");
    }

    @Test
    void handleInvalidRefreshTokenExceptionShouldReturnUnauthorizedApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/refresh");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleInvalidRefreshTokenException(
                new InvalidRefreshTokenException(),
                request
        );

        assertApiError(response, HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "/auth/refresh");
    }

    @Test
    void handlePasswordConfirmationMismatchExceptionShouldReturnBadRequestApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/users/changePassword");

        ResponseEntity<ApiError> response = globalExceptionHandler.handlePasswordConfirmationMismatchException(
                new PasswordConfirmationMismatchException(),
                request
        );

        assertApiError(response, HttpStatus.BAD_REQUEST, "PASSWORD_CONFIRMATION_MISMATCH", "/users/changePassword");
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

    @Test
    void handleMethodArgumentNotValidExceptionShouldIncludeValidationCodes() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/register");
        Method method = this.getClass().getDeclaredMethod("dummyMethodForValidation", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "registrationDTO");
        bindingResult.addError(new FieldError("registrationDTO", "email", "", false, new String[]{"NotBlank"}, null, "Email is missing"));
        bindingResult.addError(new FieldError("registrationDTO", "rowVersion", LocalDateTime.now(), false, new String[]{"NotNull"}, null, "Row version is missing"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);
        ResponseEntity<ApiError> response = globalExceptionHandler.handleMethodArgumentNotValidException(exception, request);

        assertApiError(response, HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "/auth/register");
        assertNotNull(response.getBody());
        assertInstanceOf(List.class, response.getBody().details());
        List<?> details = (List<?>) response.getBody().details();
        assertEquals(2, details.size());

        assertInstanceOf(ValidationErrorDetail.class, details.get(0));
        ValidationErrorDetail first = (ValidationErrorDetail) details.get(0);
        assertEquals("NOT_BLANK", first.code());
        assertEquals("email", first.field());

        assertInstanceOf(ValidationErrorDetail.class, details.get(1));
        ValidationErrorDetail second = (ValidationErrorDetail) details.get(1);
        assertEquals("NOT_NULL", second.code());
        assertEquals("rowVersion", second.field());
    }

    private void assertApiError(ResponseEntity<ApiError> response, HttpStatus expectedStatus, String expectedCode, String expectedPath) {
        assertEquals(expectedStatus, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedCode, response.getBody().code());
        assertEquals(expectedPath, response.getBody().path());
        assertEquals(expectedStatus.value(), response.getBody().status());
    }

    @SuppressWarnings("unused")
    private void dummyMethodForValidation(String ignored) {
    }
}
