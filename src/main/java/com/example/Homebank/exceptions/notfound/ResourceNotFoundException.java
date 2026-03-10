package com.example.Homebank.exceptions.notfound;

import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;

import java.util.Map;

/**
 * Not-found exception that carries resource metadata for client-side handling.
 */
@Getter
public class ResourceNotFoundException extends EntityNotFoundException {
    private final String resourceType;
    private final Integer resourceId;
    private final Map<String, Object> metadata;

    public ResourceNotFoundException(String resourceType, Integer resourceId, String message) {
        this(resourceType, resourceId, message, Map.of());
    }

    public ResourceNotFoundException(
            String resourceType,
            Integer resourceId,
            String message,
            Map<String, Object> metadata
    ) {
        super(message);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.metadata = metadata == null ? Map.of() : metadata;
    }
}
