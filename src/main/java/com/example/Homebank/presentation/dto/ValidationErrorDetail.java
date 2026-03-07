package com.example.Homebank.presentation.dto;

/**
 * Validation issue for a specific request field.
 *
 * @param field   The field that failed validation.
 * @param message Validation error message for the field.
 */
public record ValidationErrorDetail(
        String field,
        String message
) {
}
