package com.example.Homebank.presentation.dto.customer;

import jakarta.validation.constraints.NotBlank;

/**
 * Data transfer object for updating a customer.
 *
 * @param name        Name of the customer. Must not be blank.
 * @param description Optional description of the customer.
 */
public record CreateCustomerDTO(
        @NotBlank(message = "Name is missing") String name,
        String description
) {
}
