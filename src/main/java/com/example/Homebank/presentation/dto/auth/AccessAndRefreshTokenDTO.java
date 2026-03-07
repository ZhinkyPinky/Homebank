package com.example.Homebank.presentation.dto.auth;

public record AccessAndRefreshTokenDTO(
        String accessToken,
        String refreshToken,
        String message,
        String accountStatus
) {
}
