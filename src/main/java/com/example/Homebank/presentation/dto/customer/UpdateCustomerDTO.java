package com.example.Homebank.presentation.dto.customer;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * Data transfer object for updating a customer.
 *
 * @param name        Name of the customer. Must not be blank.
 * @param description Optional description of the customer.
 */
public record UpdateCustomerDTO(
        @NotBlank(message = "Name is missing") String name,
        String description,
        @NotBlank(message = "Row version is missing") LocalDateTime rowVersion
) {
}
