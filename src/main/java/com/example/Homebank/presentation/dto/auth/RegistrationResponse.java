package com.example.Homebank.presentation.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Response payload returned after successful registration in flows that issue tokens.
 *
 * @param accessToken  Access token.
 * @param refreshToken Refresh token.
 * @param message      Human-readable response message.
 */
public record RegistrationResponse(
        @NotBlank(message = "Access token is missing") String accessToken,
        @NotBlank(message = "Refresh token is missing") String refreshToken,
        @NotBlank(message = "Message is missing") String message) {
}
