package com.example.Homebank.presentation.dto;

import java.time.Instant;

/**
 * Standardized API error payload returned for all handled failures.
 *
 * @param timestamp Time when the error response was produced.
 * @param status    HTTP status code.
 * @param error     HTTP reason phrase.
 * @param code      Stable machine-readable application error code.
 * @param message   Human-readable description of the failure.
 * @param path      Request path for the failing request.
 * @param details   Optional structured details (e.g. validation field errors).
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        Object details
) {
}
