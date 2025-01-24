package com.example.Homebank.presentation.dto;

import java.util.List;

public record CustomersAndTransactionHeadDTO(
        List<CustomerDTO> customers,
        TransactionHeadDTO transactionHead
) {
}