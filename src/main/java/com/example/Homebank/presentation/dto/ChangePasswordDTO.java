package com.example.Homebank.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordDTO(
    @NotBlank(message = "Refresh token is missing") String refreshToken,
       @NotBlank(message = "Old password is missing") String oldPassword,
       @NotBlank(message = "New password is missing") String newPassword,
       @NotBlank(message = "New password confirmation is missing") String confirmNewPassword
) {
}
