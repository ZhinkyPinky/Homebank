package com.example.Homebank.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record SignOutRequest(@NotBlank(message = "Refresh token is missing") String refreshToken) {
}
