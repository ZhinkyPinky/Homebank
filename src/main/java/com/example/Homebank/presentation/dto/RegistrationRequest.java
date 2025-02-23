package com.example.Homebank.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegistrationRequest(
        @NotBlank(message = "Username is missing") String username,
        @NotBlank(message = "Password is missing") String password,
        @NotBlank(message = "Email is missing") @Email(message = "Invalid email") String email) {
}
