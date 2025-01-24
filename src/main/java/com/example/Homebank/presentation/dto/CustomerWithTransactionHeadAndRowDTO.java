package com.example.Homebank.presentation.dto;

public record CustomerWithTransactionHeadAndRowDTO(
        CustomerDTO customer,
        TransactionHeadDTO transactionHead,
        TransactionRowDTO transactionRow) {
}