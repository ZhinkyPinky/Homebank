package com.example.Homebank.presentation.dto;

public record ChangePasswordDTO(
        String refreshToken,
        String oldPassword,
        String newPassword,
        String confirmNewPassword
) {
}
