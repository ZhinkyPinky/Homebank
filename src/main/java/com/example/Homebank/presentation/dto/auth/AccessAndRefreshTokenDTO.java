package com.example.Homebank.presentation.dto.auth;

import java.time.Duration;

/**
 * Internal authentication result containing an access token, optional refresh token, and account status.
 *
 * @param accessToken   Access token used for authenticated API calls.
 * @param refreshToken  Refresh token used to obtain new tokens; null when signing in pending activation.
 * @param refreshTokenDuration Duration of the refresh token; null when no refresh token is issued.
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
