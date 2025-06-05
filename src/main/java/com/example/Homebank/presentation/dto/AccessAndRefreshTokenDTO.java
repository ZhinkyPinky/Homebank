package com.example.Homebank.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record AccessAndRefreshTokenDTO(
         String accessToken,
         String refreshToken,
        String message
) {
}
