package com.example.Homebank.presentation.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for receiving a recovery token from the client.
 *
 * @param token Recovery token.
 */
public record RecoveryTokenDTO(@NotBlank(message = "Recovery token is missing") String token) {
}
