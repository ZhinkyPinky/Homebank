package com.example.Homebank.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegistrationRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank @Email String email) {
}
