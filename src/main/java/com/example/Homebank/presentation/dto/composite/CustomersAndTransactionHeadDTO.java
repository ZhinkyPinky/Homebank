package com.example.Homebank.presentation.dto.composite;

import com.example.Homebank.presentation.dto.transactionhead.TransactionHeadDTO;
import com.example.Homebank.presentation.dto.customer.CustomerDTO;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CustomersAndTransactionHeadDTO(
        List<CustomerDTO> customers,
        @NotNull(message = "Transaction head is missing") TransactionHeadDTO transactionHead
) {
}