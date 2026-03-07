package com.example.Homebank.presentation.dto.composite;

import com.example.Homebank.presentation.dto.transactionhead.TransactionHeadDTO;
import com.example.Homebank.presentation.dto.customer.CustomerDTO;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CustomerAndTransactionHeadsDTO(
        @NotNull(message = "Customer is missing") CustomerDTO customer,
        List<TransactionHeadDTO> transactionHeads) {
}
