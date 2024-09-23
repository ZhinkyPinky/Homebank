package com.example.Homebank.presentation.bodies;

import com.example.Homebank.dataAccess.entities.Customer;
import com.example.Homebank.dataAccess.entities.TransactionHead;
import com.example.Homebank.dataAccess.entities.TransactionRow;
import lombok.Data;

import java.util.List;

@Data
public class CustomerAndTransactionHeadAndTransactionRow {
    private Customer customer;

    private TransactionHead transactionHead;

    private TransactionRow transactionRow;

    public CustomerAndTransactionHeadAndTransactionRow(Customer customer, TransactionHead transactionHead, TransactionRow transactionRow){
        this.customer = customer;
        this.transactionHead = transactionHead;
        this.transactionRow = transactionRow;
    }
}