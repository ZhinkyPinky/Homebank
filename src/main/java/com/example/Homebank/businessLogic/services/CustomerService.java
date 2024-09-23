package com.example.Homebank.businessLogic.services;

import com.example.Homebank.dataAccess.entities.Customer;
import com.example.Homebank.dataAccess.entities.TransactionHead;
import com.example.Homebank.dataAccess.entities.TransactionRow;
import com.example.Homebank.dataAccess.repositories.CustomerRepository;
import com.example.Homebank.presentation.bodies.CustomerAndTransactionHead;
import com.example.Homebank.presentation.bodies.CustomerAndTransactionHeadAndTransactionRow;
import com.example.Homebank.presentation.bodies.CustomerAndTransactionHeadAndTransactionRows;
import com.example.Homebank.presentation.bodies.CustomerAndTransactionHeads;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionHeadService transactionHeadService;

    @Autowired
    private TransactionRowService transactionRowService;

    public ResponseEntity<List<Customer>> getAll() {
        return ResponseEntity.ok().body(customerRepository.findAll());
    }

    public Customer getCustomer(long customerId) {
        return customerRepository.findById(customerId).orElseThrow(() -> new EntityNotFoundException("The customer could not be found."));
    }

    public CustomerAndTransactionHeads getCustomerAndTransactionHeads(long customerId) {
        Customer customer = getCustomer(customerId);
        List<TransactionHead> transactionHeads = transactionHeadService.getTransactionHeadsByCustomerId(customerId);

        return new CustomerAndTransactionHeads(customer, transactionHeads);
    }

    public CustomerAndTransactionHead getCustomerAndTransactionHead(long customerId, long transactionHeadId) {
        Customer customer = getCustomer(customerId);
        TransactionHead transactionHead = transactionHeadService.getTransactionHead(transactionHeadId); //TODO: Make sure that customer id match lender/borrower id?

        return new CustomerAndTransactionHead(customer, transactionHead);
    }

    public CustomerAndTransactionHeadAndTransactionRows getCustomerTransactionHeadAndRows(long customerId, long transactionHeadId) {
        Customer customer = getCustomer(customerId);
        TransactionHead transactionHead = transactionHeadService.getTransactionHead(transactionHeadId); //TODO: Make sure that customer id match lender/borrower id?
        List<TransactionRow> transactionRows = transactionRowService.getAllByTransactionHeadId(transactionHeadId);

        return new CustomerAndTransactionHeadAndTransactionRows(customer, transactionHead, transactionRows);
    }

    public CustomerAndTransactionHeadAndTransactionRow getCustomerAndTransactionHeadAndTransactionRow(long customerId, long transactionHeadId, long transactionRowId) {
        Customer customer = getCustomer(customerId);
        TransactionHead transactionHead = transactionHeadService.getTransactionHead(transactionHeadId); //TODO: Make sure that customer id match lender/borrower id?
        TransactionRow transactionRow = transactionRowService.getTransactionRow(transactionRowId);

        return new CustomerAndTransactionHeadAndTransactionRow(customer, transactionHead, transactionRow);
    }
}