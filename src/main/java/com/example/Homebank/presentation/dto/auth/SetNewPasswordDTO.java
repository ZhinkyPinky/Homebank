package com.example.Homebank.presentation.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for resetting a password through account recovery.
 *
 * @param recoveryToken      Recovery token proving password-reset authorization.
 * @param newPassword        New password to set.
 * @param confirmNewPassword Confirmation of the new password.
 */
public record SetNewPasswordDTO(
        @NotBlank(message = "Recovery token is missing") String recoveryToken,
        @NotBlank(message = "New password is missing") String newPassword,
        @NotBlank(message = "New password confirmation is missing") String confirmNewPassword
) {
}
