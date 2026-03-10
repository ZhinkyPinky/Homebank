package com.example.Homebank.presentation.dto;

import java.util.Map;

/**
 * Structured details for resource-level not found responses.
 *
 * @param resourceType Resource type that was not found.
 * @param resourceId   Resource identifier.
 * @param metadata     Optional additional context for the lookup that failed.
 */
public record ResourceNotFoundDetail(
        String resourceType,
        Integer resourceId,
        Map<String, Object> metadata
) {
}
