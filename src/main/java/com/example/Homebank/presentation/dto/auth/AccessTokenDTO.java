package com.example.Homebank.presentation.dto.auth;

public record AccessTokenDTO(
        String accessToken,
        String message,
        String accountStatus
) {
}
