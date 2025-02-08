package com.example.Homebank.presentation.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CustomerWithTransactionHeadAndRowsDTO(
        @NotNull(message = "Customer is missing") CustomerDTO customer,
        @NotNull(message = "Transaction head is missing") TransactionHeadDTO transactionHead,
        List<TransactionRowDTO> transactionRows) {
}
