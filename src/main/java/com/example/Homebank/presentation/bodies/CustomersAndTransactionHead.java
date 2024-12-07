package com.example.Homebank.presentation.bodies;

import com.example.Homebank.dataAccess.entities.Customer;
import com.example.Homebank.dataAccess.entities.TransactionHead;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Supplier;

@Data
@NoArgsConstructor
public class CustomersAndTransactionHead {
    private List<Customer> customers;
    private TransactionHead transactionHead;

    public CustomersAndTransactionHead(List<Customer> customers, TransactionHead transactionHead) {
        this.customers = customers;
        this.transactionHead = transactionHead;
    }
}