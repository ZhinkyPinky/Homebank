package com.example.Homebank.presentation.controllers;

import com.example.Homebank.dataAccess.entities.Customer;
import com.example.Homebank.businessLogic.services.CustomerService;
import com.example.Homebank.presentation.bodies.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.Dispatcher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<Customer>> getCustomers() {
        List<Customer> body = customerService.getCustomers();
        
        return ResponseEntity.ok(body);
    }

    @GetMapping("/transactionHeads/{transactionHeadId}")
    public ResponseEntity<CustomersAndTransactionHead> getCustomersAndTransactionHead(@PathVariable final long transactionHeadId) {
        CustomersAndTransactionHead body = customerService.getCustomersAndTransactionHead(transactionHeadId);

        return ResponseEntity.ok(body);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> getCustomer(@PathVariable final long customerId) {
        Customer body = customerService.getCustomer(customerId);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{customerId}/transactionHeads")
    public ResponseEntity<CustomerAndTransactionHeads> getCustomerAndTransactionHeads(@PathVariable final long customerId) {
        CustomerAndTransactionHeads body = customerService.getCustomerAndTransactionHeads(customerId);

        return ResponseEntity.ok(body);
    }

    @GetMapping("/{customerId}/transactionHeads/{transactionHeadId}")
    public ResponseEntity<CustomerAndTransactionHead> getCustomerAndTransactionHead(@PathVariable final long customerId, @PathVariable final long transactionHeadId) {
        CustomerAndTransactionHead body = customerService.getCustomerAndTransactionHead(customerId, transactionHeadId);

        return ResponseEntity.ok(body);
    }

    @GetMapping("/{customerId}/transactionHeads/{transactionHeadId}/transactionRows")
    public ResponseEntity<CustomerAndTransactionHeadAndTransactionRows> getCustomerAndTransactionHeadAndRows(@PathVariable final long customerId, @PathVariable final long transactionHeadId) {
        CustomerAndTransactionHeadAndTransactionRows body = customerService.getCustomerTransactionHeadAndRows(customerId, transactionHeadId);

        return ResponseEntity.ok(body);
    }

    @GetMapping("/{customerId}/transactionHeads/{transactionHeadId}/transactionRows/{transactionRowId}")
    public ResponseEntity<?> getCustomerAndTransactionHeadAndTransactionRow(@PathVariable final long customerId, @PathVariable final long transactionHeadId, @PathVariable final long transactionRowId) {
        CustomerAndTransactionHeadAndTransactionRow body = customerService.getCustomerAndTransactionHeadAndTransactionRow(customerId, transactionHeadId, transactionRowId);

        return ResponseEntity.ok(body);
    }
}
