package com.example.Homebank.presentation.dto.error;

/**
 * Validation issue for a specific request field.
 *
 * @param code    Machine-readable validation code (for example NOT_BLANK or NOT_NULL).
 * @param field   The field that failed validation.
 * @param message Validation error message for the field.
 */
public record ValidationErrorDetail(
        ValidationErrorCode code,
        String field,
        String message
) {
}
