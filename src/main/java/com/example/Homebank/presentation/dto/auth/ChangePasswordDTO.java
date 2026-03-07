package com.example.Homebank.presentation.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for changing password using a valid refresh token.
 *
 * @param refreshToken      Valid refresh token for the current session.
 * @param oldPassword       Current password.
 * @param newPassword       New password to set.
 * @param confirmNewPassword Confirmation of the new password.
 */
public record ChangePasswordDTO(
        @NotBlank(message = "Refresh token is missing") String refreshToken,
        @NotBlank(message = "Old password is missing") String oldPassword,
        @NotBlank(message = "New password is missing") String newPassword,
        @NotBlank(message = "New password confirmation is missing") String confirmNewPassword
) {
}
