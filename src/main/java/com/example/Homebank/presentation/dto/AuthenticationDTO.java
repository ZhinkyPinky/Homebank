package com.example.Homebank.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthenticationDTO(
        @NotBlank(message = "Email is missing") @Email(message = "Invalid email") String email,
        @NotBlank(message = "Password is missing") String password
) {
}