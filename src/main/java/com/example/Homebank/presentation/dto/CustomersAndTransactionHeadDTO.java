package com.example.Homebank.presentation.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CustomersAndTransactionHeadDTO(
        List<CustomerDTO> customers,
        @NotNull(message = "Transaction head is missing") TransactionHeadDTO transactionHead
) {
}