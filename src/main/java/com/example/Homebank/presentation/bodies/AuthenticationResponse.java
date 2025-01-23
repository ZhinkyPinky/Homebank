package com.example.Homebank.presentation.bodies;

public record AuthenticationResponse(
        String accessToken,
        String refreshToken,
        String message) {
}
