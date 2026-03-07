package com.example.Homebank.presentation.dto.composite;

import com.example.Homebank.presentation.dto.transactionhead.TransactionHeadDTO;
import com.example.Homebank.presentation.dto.transactionhead.TransactionRowDTO;
import com.example.Homebank.presentation.dto.customer.CustomerDTO;
import jakarta.validation.constraints.NotNull;

public record CustomerWithTransactionHeadAndRowDTO(
        @NotNull(message = "Customer is missing") CustomerDTO customer,
        @NotNull(message = "Transaction head is missing") TransactionHeadDTO transactionHead,
        @NotNull(message = "Transaction row is missing") TransactionRowDTO transactionRow) {
}