package com.example.Homebank.presentation.dto;

public record AccessAndRefreshTokenDTO(
        String accessToken,
        String refreshToken,
        String message,
        String accountStatus
) {
}
