package com.example.Homebank.presentation.dto;

public record SetNewPasswordDTO(
        String recoveryToken,
        String newPassword,
        String confirmNewPassword
) {
}
