package com.example.Homebank.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record RegistrationResponse(
        @NotBlank(message = "Access token is missing") String accessToken,
        @NotBlank(message = "Refresh token is missing") String refreshToken,
        @NotBlank(message = "Message is missing") String message) {
}
