package com.example.Homebank.presentation.dto;

import java.util.List;

public record CustomerWithTransactionHeadAndRowsDTO(
        CustomerDTO customer,
        TransactionHeadDTO transactionHead,
        List<TransactionRowDTO> transactionRows) {
}
