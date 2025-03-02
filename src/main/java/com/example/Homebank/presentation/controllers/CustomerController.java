package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.CustomerService;
import com.example.Homebank.presentation.ApiPaths;
import com.example.Homebank.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.CUSTOMERS)
public class CustomerController {
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getCustomers() {
        logger.info("Request to get customers received.");
        
        List<CustomerDTO> body = customerService.getCustomers();
        return ResponseEntity.ok(body);
    }

    //TODO: Move to TransactionHeadController? Find solution.
    @GetMapping("/transactionHeads/{transactionHeadId}")
    public ResponseEntity<CustomersAndTransactionHeadDTO> getCustomersAndTransactionHead(@PathVariable final long transactionHeadId) {
        logger.info("Request to get customers and transaction head with transactionHeadId: {} received.", transactionHeadId);

        CustomersAndTransactionHeadDTO body = customerService.getCustomersAndTransactionHead(transactionHeadId);
        return ResponseEntity.ok(body);
    }

    @GetMapping(ApiPaths.CUSTOMER)
    public ResponseEntity<CustomerDTO> getCustomer(@PathVariable final long customerId) {
        logger.info("Request to get customer with ID: {} received.", customerId);

        CustomerDTO body = customerService.getCustomer(customerId);
        return ResponseEntity.ok(body);
    }

    @GetMapping(ApiPaths.CUSTOMER_WITH_TRANSACTION_HEADS)
    public ResponseEntity<CustomerAndTransactionHeadsDTO> getCustomerAndTransactionHeads(@PathVariable final long customerId) {
        logger.info("Request to get customer and transaction heads with customerId: {} received.", customerId);

        CustomerAndTransactionHeadsDTO body = customerService.getCustomerAndTransactionHeads(customerId);
        return ResponseEntity.ok(body);
    }

    @GetMapping(ApiPaths.CUSTOMER_WITH_TRANSACTION_HEAD)
    public ResponseEntity<CustomerAndTransactionHeadDTO> getCustomerAndTransactionHead(@PathVariable final long customerId, @PathVariable final long transactionHeadId) {
        logger.info("Request to get customer with ID: {} and transaction head with ID: {} received.", customerId, transactionHeadId);

        CustomerAndTransactionHeadDTO body = customerService.getCustomerAndTransactionHead(customerId, transactionHeadId);
        return ResponseEntity.ok(body);
    }

    @GetMapping(ApiPaths.CUSTOMER_WITH_TRANSACTION_HEAD_AND_ROWS)
    public ResponseEntity<CustomerWithTransactionHeadAndRowsDTO> getCustomerAndTransactionHeadAndRows(@PathVariable final long customerId, @PathVariable final long transactionHeadId) {
        logger.info("Request to get customer, transaction head, and rows for customerId: {} and transactionHeadId: {} received.", customerId, transactionHeadId);

        CustomerWithTransactionHeadAndRowsDTO body = customerService.getCustomerTransactionHeadAndRows(customerId, transactionHeadId);
        return ResponseEntity.ok(body);
    }

    @GetMapping(ApiPaths.CUSTOMER_WITH_TRANSACTION_HEAD_AND_ROW)
    public ResponseEntity<?> getCustomerAndTransactionHeadAndTransactionRow(@PathVariable final long customerId, @PathVariable final long transactionHeadId, @PathVariable final long transactionRowId) {
        logger.info("Request to get customer, transaction head, and row for customerId: {}, transactionHeadId: {}, and transactionRowId: {} received.", customerId, transactionHeadId, transactionRowId);

        CustomerWithTransactionHeadAndRowDTO body = customerService.getCustomerAndTransactionHeadAndTransactionRow(customerId, transactionHeadId, transactionRowId);
        return ResponseEntity.ok(body);
    }
}
