package com.example.Homebank.presentation.dto.error;

/**
 * Validation codes used in {@link ValidationErrorDetail}.
 */
public enum ValidationErrorCode {
    NOT_NULL,
    NOT_BLANK,
    NOT_EMPTY,
    SIZE,
    PATTERN,
    EMAIL,
    PAST,
    PAST_OR_PRESENT,
    FUTURE,
    FUTURE_OR_PRESENT,
    MIN,
    MAX,
    POSITIVE,
    POSITIVE_OR_ZERO,
    NEGATIVE,
    NEGATIVE_OR_ZERO,
    DIGITS,
    ASSERT_TRUE,
    ASSERT_FALSE,
    VALIDATION_ERROR,
    UNKNOWN
}
