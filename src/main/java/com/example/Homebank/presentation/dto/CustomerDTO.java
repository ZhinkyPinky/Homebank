package com.example.Homebank.presentation.dto;

import com.example.Homebank.dataAccess.entities.CustomerEntity;

import java.time.LocalDateTime;

public record CustomerDTO(
        Long id,
        String name,
        String description,
        String typeOfCustomerCode,
        int customerAmount,
        LocalDateTime rowVersion
) {
    public static CustomerDTO fromEntity(CustomerEntity entity) {
        return new CustomerDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getTypeOfCustomerCode(),
                entity.getCustomerAmount(),
                entity.getRowVersion());
    }
}
