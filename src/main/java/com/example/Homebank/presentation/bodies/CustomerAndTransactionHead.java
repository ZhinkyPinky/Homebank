package com.example.Homebank.presentation.bodies;

import com.example.Homebank.dataAccess.entities.Customer;
import com.example.Homebank.dataAccess.entities.TransactionHead;
import lombok.Data;
import lombok.RequiredArgsConstructor;

public record CustomerAndTransactionHead(Customer customer, TransactionHead transactionHead) {
}
