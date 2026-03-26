package com.example.Homebank.presentation.dto.error;

import com.example.Homebank.exceptions.authentication.TokenType;

/**
 * Structured details for token-related failures.
 *
 * @param tokenType Token category involved in the failure.
 */
public record TokenErrorDetail(
        TokenType tokenType
) {
}
