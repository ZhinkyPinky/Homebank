package com.example.Homebank.presentation.bodies;

import com.example.Homebank.dataAccess.entities.Customer;
import com.example.Homebank.dataAccess.entities.TransactionHead;
import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
public class CustomerAndTransactionHeads {
    private Customer customer;
    private List<TransactionHead> transactionHeads;

    public CustomerAndTransactionHeads(Customer customer, List<TransactionHead> transactionHeads) {
        this.customer = customer;
        this. transactionHeads = transactionHeads;
    }
}
