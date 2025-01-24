package com.example.Homebank.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequestDTO(
        @NotBlank String username,
        @NotBlank String password
) { }