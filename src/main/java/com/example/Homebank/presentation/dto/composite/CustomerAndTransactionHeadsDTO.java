package com.example.Homebank.presentation.dto.composite;

import com.example.Homebank.presentation.dto.transactionhead.TransactionHeadDTO;
import com.example.Homebank.presentation.dto.customer.CustomerDTO;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Composite response containing one customer and all related transaction heads.
 *
 * @param customer         Customer details.
 * @param transactionHeads Transaction heads related to the customer.
 */
public record CustomerAndTransactionHeadsDTO(
        @NotNull(message = "Customer is missing") CustomerDTO customer,
        List<TransactionHeadDTO> transactionHeads) {
}
