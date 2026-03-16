package com.example.Homebank.presentation.dto.error;

/**
 * Structured details for token-related failures.
 *
 * @param tokenType Token category involved in the failure.
 */
public record TokenErrorDetail(
        String tokenType
) {
}
