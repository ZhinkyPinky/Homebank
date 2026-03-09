package com.example.Homebank.presentation.dto;

import java.util.Map;

/**
 * Structured details for resource-level access denied responses.
 *
 * @param resourceType Resource type that access was denied to.
 * @param resourceId   Resource identifier.
 * @param action       Action that was denied.
 * @param metadata     Optional additional context for the denial decision.
 */
public record ResourceAccessDeniedDetail(
        String resourceType,
        Integer resourceId,
        String action,
        Map<String, Object> metadata
) {
}
