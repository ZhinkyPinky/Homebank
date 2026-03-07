package com.example.Homebank.presentation.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload containing an e-mail address.
 *
 * @param email E-mail address.
 */
public record EmailDTO(
        @NotBlank(message = "Email is missing")
        @Email(message = "Invalid email")
        String email
) {
}
