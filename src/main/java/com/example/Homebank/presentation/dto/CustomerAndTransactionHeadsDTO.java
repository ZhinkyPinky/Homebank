package com.example.Homebank.presentation.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CustomerAndTransactionHeadsDTO(
        @NotNull(message = "Customer is missing") CustomerDTO customer,
        List<TransactionHeadDTO> transactionHeads) {
}
