package com.example.Homebank.presentation.bodies;

import com.example.Homebank.dataAccess.entities.Customer;
import com.example.Homebank.dataAccess.entities.TransactionHead;
import com.example.Homebank.dataAccess.entities.TransactionRow;
import lombok.Data;
import lombok.RequiredArgsConstructor;

public record CustomerAndTransactionHeadAndTransactionRow(
        Customer customer,
        TransactionHead transactionHead,
        TransactionRow transactionRow) {
}