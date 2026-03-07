package com.example.Homebank.presentation.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload containing a refresh token.
 *
 * @param refreshToken Refresh token.
 */
public record RefreshTokenDTO(@NotBlank(message = "Refresh token is missing") String refreshToken) {
}
