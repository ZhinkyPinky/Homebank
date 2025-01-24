package com.example.Homebank.presentation.dto;

public record CustomerAndTransactionHeadDTO(
        CustomerDTO customer,
        TransactionHeadDTO transactionHead
) {
}
