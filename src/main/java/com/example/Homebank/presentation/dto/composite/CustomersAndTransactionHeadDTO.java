package com.example.Homebank.presentation.dto.composite;

import com.example.Homebank.presentation.dto.transactionhead.TransactionHeadDTO;
import com.example.Homebank.presentation.dto.customer.CustomerDTO;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Composite response containing multiple customers and one transaction head.
 *
 * @param customers       Customer list.
 * @param transactionHead Transaction head details.
 */
public record CustomersAndTransactionHeadDTO(
        List<CustomerDTO> customers,
        @NotNull(message = "Transaction head is missing") TransactionHeadDTO transactionHead
) {
}
