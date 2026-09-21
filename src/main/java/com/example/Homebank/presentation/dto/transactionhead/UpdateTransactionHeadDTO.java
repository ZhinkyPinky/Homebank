package com.example.Homebank.presentation.dto.transactionhead;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Editable fields. The path identifies the head; its lender and borrower remain unchanged. */
public record UpdateTransactionHeadDTO(
        @NotBlank(message = "Transaction name is missing") String transactionName,
        String description,
        @NotNull(message = "Start date is missing") LocalDate startDate,
        LocalDate prelEndDate,
        LocalDate endDate,
        @NotNull(message = "Row version is missing") LocalDateTime rowVersion
) {}
