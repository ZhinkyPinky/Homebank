package com.example.Homebank.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record RecoveryTokenDTO(@NotBlank(message = "Recovery token is missing") String token) {
}
