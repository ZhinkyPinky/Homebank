package com.example.Homebank.exceptions.authentication;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

/**
 * Custom authentication failure handler that returns a JSON response with an error message when authentication fails.
 */
@Component
public class AuthenticationExceptionHandler implements AuthenticationFailureHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handles authentication failure by returning a JSON response with an error code and message.
     *
     * @param request   The HTTP request that resulted in an authentication failure.
     * @param response  The HTTP response to be sent back to the client.
     * @param exception The exception that caused the authentication failure.
     * @throws IOException      If an input or output error occurs while writing the response.
     * @throws ServletException If a servlet error occurs while handling the authentication failure.
     */
    @Override
    public void onAuthenticationFailure(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String code = switch (exception) {
            case AccountNotActivatedException e -> "ACCOUNT_NOT_ACTIVATED";
            case ActivationTokenExpiredException e -> "ACTIVATION_TOKEN_EXPIRED";
            case BadCredentialsException e -> "BAD_CREDENTIALS";
            default -> "AUTHENTICATION_FAILED";
        };

        objectMapper.writeValue(response.getWriter(), Map.of(
                "error", code,
                "message", "Authentication failed."
        ));
    }
}
