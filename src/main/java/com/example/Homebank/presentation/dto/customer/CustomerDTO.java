package com.example.Homebank.presentation.dto.customer;

import com.example.Homebank.dataAccess.views.CustomerView;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Customer data exposed by read endpoints.
 *
 * @param id                 Customer identifier.
 * @param name               Customer name.
 * @param description        Optional description.
 * @param typeOfCustomerCode Customer type code.
 * @param customerAmount     Aggregated customer amount.
 * @param rowVersion         Concurrency/version timestamp.
 */
public record CustomerDTO(
        @NotNull(message = "Id missing") Integer id,
        @NotBlank(message = "Name is missing") String name,
        String description,
        @NotBlank(message = "Customer code missing") String typeOfCustomerCode,
        @NotNull(message = "Customer amount is missing") Integer customerAmount,
        @NotNull(message = "Row version is missing") LocalDateTime rowVersion
) {
    public static CustomerDTO fromEntity(CustomerView entity) {
        return new CustomerDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getTypeOfCustomerCode(),
                entity.getCustomerAmount(),
                entity.getRowVersion());
    }
}
