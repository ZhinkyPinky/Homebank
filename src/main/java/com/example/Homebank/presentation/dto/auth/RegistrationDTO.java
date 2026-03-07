package com.example.Homebank.presentation.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for user registration.
 *
 * @param email    User e-mail address.
 * @param password User password.
 */
public record RegistrationDTO(
        @NotBlank(message = "Email is missing") @Email(message = "Invalid email") String email,
        @NotBlank(message = "Password is missing") String password) {
}
