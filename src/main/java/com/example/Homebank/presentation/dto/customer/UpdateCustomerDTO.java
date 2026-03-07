package com.example.Homebank.presentation.dto.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Data transfer object for updating a customer.
 *
 * @param name        Name of the customer. Must not be blank.
 * @param description Optional description of the customer.
 * @param rowVersion  Current row version used for optimistic locking.
 */
public record UpdateCustomerDTO(
        @NotBlank(message = "Name is missing") String name,
        String description,
        @NotNull(message = "Row version is missing") LocalDateTime rowVersion
) {
}
