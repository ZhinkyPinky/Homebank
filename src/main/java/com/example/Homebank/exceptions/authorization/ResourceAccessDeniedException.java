package com.example.Homebank.exceptions.authorization;

import lombok.Getter;
import org.springframework.security.access.AccessDeniedException;

import java.util.Map;

/**
 * Access-denied exception that carries resource metadata for client-side handling.
 */
@Getter
public class ResourceAccessDeniedException extends AccessDeniedException {
    private final String resourceType;
    private final Integer resourceId;
    private final String action;
    private final Map<String, Object> metadata;

    public ResourceAccessDeniedException(String resourceType, Integer resourceId, String action, String message) {
        this(resourceType, resourceId, action, message, Map.of());
    }

    public ResourceAccessDeniedException(
            String resourceType,
            Integer resourceId,
            String action,
            String message,
            Map<String, Object> metadata
    ) {
        super(message);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.action = action;
        this.metadata = metadata == null ? Map.of() : metadata;
    }
}
