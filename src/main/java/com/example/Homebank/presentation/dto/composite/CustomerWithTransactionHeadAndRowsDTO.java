package com.example.Homebank.presentation.dto.composite;

import com.example.Homebank.presentation.dto.transactionhead.TransactionHeadDTO;
import com.example.Homebank.presentation.dto.transactionrow.TransactionRowDTO;
import com.example.Homebank.presentation.dto.customer.CustomerDTO;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Composite response containing customer, transaction head, and related transaction rows.
 *
 * @param customer        Customer details.
 * @param transactionHead Transaction head details.
 * @param transactionRows Transaction rows belonging to the transaction head.
 */
public record CustomerWithTransactionHeadAndRowsDTO(
        @NotNull(message = "Customer is missing") CustomerDTO customer,
        @NotNull(message = "Transaction head is missing") TransactionHeadDTO transactionHead,
        List<TransactionRowDTO> transactionRows) {
}
