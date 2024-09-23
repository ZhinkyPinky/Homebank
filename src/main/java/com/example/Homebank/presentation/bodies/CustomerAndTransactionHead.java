package com.example.Homebank.presentation.bodies;

import com.example.Homebank.dataAccess.entities.Customer;
import com.example.Homebank.dataAccess.entities.TransactionHead;
import lombok.Data;

@Data
public class CustomerAndTransactionHead {
    private Customer customer;
    private TransactionHead transactionHead;

    public CustomerAndTransactionHead(Customer customer, TransactionHead transactionHead){
        this.customer = customer;
        this.transactionHead = transactionHead;
    }
}
