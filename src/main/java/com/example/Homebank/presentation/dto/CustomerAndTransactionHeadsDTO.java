package com.example.Homebank.presentation.dto;

import java.util.List;

public record CustomerAndTransactionHeadsDTO(
        CustomerDTO customer,
        List<TransactionHeadDTO> transactionHeads) {
}
