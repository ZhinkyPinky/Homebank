package com.example.Homebank.presentation.controllers;

import com.example.Homebank.presentation.dto.ApiError;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
