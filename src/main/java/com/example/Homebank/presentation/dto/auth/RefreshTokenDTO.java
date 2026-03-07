package com.example.Homebank.presentation.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenDTO(@NotBlank(message = "Refresh token is missing") String refreshToken) {
}
