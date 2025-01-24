package com.example.Homebank.presentation.dto;

public record AuthenticationResponseDTO(
        String accessToken,
        String refreshToken,
        String message
) { }
