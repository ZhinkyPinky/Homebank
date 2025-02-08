package com.example.Homebank.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record CustomerAndTransactionHeadDTO(
        @NotNull(message = "Customer is missing") CustomerDTO customer,
        @NotNull(message = "Transaction head is missing") TransactionHeadDTO transactionHead
) {
}
