package com.example.Homebank.presentation.dto.transactionhead;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** Creation fields. The database assigns the ID and row version. */
public record CreateTransactionHeadDTO(
        @NotNull(message = "Lender id is missing") Integer lenderId,
        @NotNull(message = "Borrower id is missing") Integer borrowerId,
        @NotBlank(message = "Transaction name is missing") String transactionName,
        String description,
        @NotNull(message = "Start date is missing") LocalDate startDate,
        LocalDate prelEndDate,
        LocalDate endDate
) {}
