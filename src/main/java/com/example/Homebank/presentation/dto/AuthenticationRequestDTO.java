package com.example.Homebank.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequestDTO(
        @NotBlank(message = "Username is missing") String username,
        @NotBlank(message = "Password is missing") String password
) { }