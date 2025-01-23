package com.example.Homebank.presentation.bodies;

import com.example.Homebank.dataAccess.entities.Customer;
import com.example.Homebank.dataAccess.entities.TransactionHead;
import com.example.Homebank.dataAccess.entities.TransactionRow;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

public record CustomerAndTransactionHeadAndTransactionRows(
        Customer customer,
        TransactionHead transactionHead,
        List<TransactionRow> transactionRows) {
}
