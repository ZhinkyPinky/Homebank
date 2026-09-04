package com.example.Homebank.presentation.dto.auth;

import java.time.Duration;

/**
 * Authentication response payload containing token pair and account status.
 *
 * @param accessToken   Access token used for authenticated API calls.
 * @param refreshToken  Refresh token used to obtain new tokens.
 * @param refreshTokenDuration Duration of the refresh token.
 * @param message       Human-readable message describing the authentication result.
 * @param accountStatus Current account status for client-side routing/handling.
 */
public record AccessAndRefreshTokenDTO(
        String accessToken,
        String refreshToken,
        Duration refreshTokenDuration,
        String message,
        String accountStatus
) {
}
