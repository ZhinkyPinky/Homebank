package com.example.Homebank.presentation.bodies;

import com.example.Homebank.dataAccess.entities.Customer;
import com.example.Homebank.dataAccess.entities.TransactionHead;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

public record CustomersAndTransactionHead(
        List<Customer> customers,
        TransactionHead transactionHead) {
}