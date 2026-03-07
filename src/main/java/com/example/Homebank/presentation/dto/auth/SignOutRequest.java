package com.example.Homebank.presentation.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for signing out from a refresh-token-backed session.
 *
 * @param refreshToken Refresh token that should be invalidated.
 */
public record SignOutRequest(@NotBlank(message = "Refresh token is missing") String refreshToken) {
}
