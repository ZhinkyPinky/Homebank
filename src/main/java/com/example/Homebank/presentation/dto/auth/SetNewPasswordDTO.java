package com.example.Homebank.presentation.dto.auth;

public record SetNewPasswordDTO(
        String recoveryToken,
        String newPassword,
        String confirmNewPassword
) {
}
