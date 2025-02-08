package com.example.Homebank.presentation.dto;

import com.example.Homebank.dataAccess.views.CustomerView;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CustomerDTO(
        @NotNull(message = "Id missing") Long id,
        @NotBlank(message = "Name is missing") String name,
        String description,
        @NotBlank(message = "Customer code missing") String typeOfCustomerCode,
        @NotNull(message = "Customer amount is missing") int customerAmount,
        @NotBlank(message = "Row version is missing") LocalDateTime rowVersion
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
