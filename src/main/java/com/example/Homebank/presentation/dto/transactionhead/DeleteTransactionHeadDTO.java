package com.example.Homebank.presentation.dto.transactionhead;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record DeleteTransactionHeadDTO(
        @NotNull(message = "Id is missing") Integer id,
        @NotNull(message = "Row version is missing") LocalDateTime rowVersion
) {
}
