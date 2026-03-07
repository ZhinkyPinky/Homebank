package com.example.Homebank.presentation.dto.composite;

import com.example.Homebank.presentation.dto.transactionhead.TransactionHeadDTO;
import com.example.Homebank.presentation.dto.customer.CustomerDTO;
import jakarta.validation.constraints.NotNull;

/**
 * Composite response containing one customer and one transaction head.
 *
 * @param customer        Customer details.
 * @param transactionHead Transaction head details.
 */
public record CustomerAndTransactionHeadDTO(
        @NotNull(message = "Customer is missing") CustomerDTO customer,
        @NotNull(message = "Transaction head is missing") TransactionHeadDTO transactionHead
) {
}
