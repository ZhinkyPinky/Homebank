package com.example.Homebank.presentation.bodies;

import com.example.Homebank.dataAccess.entities.Customer;
import com.example.Homebank.dataAccess.entities.TransactionHead;
import com.example.Homebank.dataAccess.entities.TransactionRow;
import lombok.Data;

import java.util.List;

@Data
public class CustomerAndTransactionHeadAndTransactionRows {
    private Customer customer;

    private TransactionHead transactionHead;

    private List<TransactionRow> transactionRows;

    public CustomerAndTransactionHeadAndTransactionRows(Customer customer, TransactionHead transactionHead, List<TransactionRow> transactionRows){
        this.customer = customer;
        this.transactionHead = transactionHead;
        this.transactionRows = transactionRows;
    }
}
