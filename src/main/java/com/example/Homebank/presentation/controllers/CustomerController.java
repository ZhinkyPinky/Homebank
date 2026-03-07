package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.CustomerService;
import com.example.Homebank.presentation.ApiPaths;
import com.example.Homebank.presentation.dto.composite.*;
import com.example.Homebank.presentation.dto.customer.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.CUSTOMERS)
public class CustomerController {
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    //TODO: Return created/updated customer and location.

    /**
     * Handles requests to create a new customer.
     *
     * @param customer Customer data.
     * @return Response indicating whether the customer was successfully created.
     */
    @PostMapping
    public ResponseEntity<String> createCustomer(@Valid @RequestBody CreateCustomerDTO customer) {
        logger.info("Request to create customer received.");
        //TODO: Return created customer and location.
        customerService.createCustomer(customer);

        return ResponseEntity.ok().build();
    }

    /**
     * Handles requests to update a customer.
     *
     * @param customerId ID of the customer to update.
     * @param customer   Updated customer data.
     * @return Response indicating whether the customer was successfully updated.
     */
    @PutMapping(ApiPaths.CUSTOMER)
    public ResponseEntity<String> updateCustomer(@PathVariable int customerId, @Valid @RequestBody UpdateCustomerDTO customer) {
        logger.info("Request to update customer with ID: {} received.", customerId);

        customerService.updateCustomer(customerId, customer);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(ApiPaths.CUSTOMER)
    public ResponseEntity<String> deleteCustomer(@PathVariable int customerId) {
        logger.info("Request to delete customer with ID: {} received.", customerId);

        //TODO: Implement deleteCustomer method in CustomerService and uncomment the line below.
        //customerService.deleteCustomer(customerId);

        //Not implemented yet.
        return ResponseEntity.status(501).body("Deleting customers is not implemented yet.");
    }

    /**
     * Handles requests to retrieve all customers.
     *
     * @return All customers.
     */
    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getCustomers() {
        logger.info("Request to get customers received.");

        List<CustomerDTO> body = customerService.getCustomers();
        return ResponseEntity.ok(body);
    }


    /**
     * Handles requests to retrieve all customers and a specified transaction head.
     *
     * @param transactionHeadId ID of the transaction head.
     * @return All customers and the specified transaction head.
     */
    @GetMapping("/transactionHeads/{transactionHeadId}")
    public ResponseEntity<CustomersAndTransactionHeadDTO> getCustomersAndTransactionHead(
            @PathVariable final int transactionHeadId) {
        //TODO: Move to TransactionHeadController? Find solution.
        logger.info("Request to get customers and transaction head with transactionHeadId: {} received.", transactionHeadId);

        CustomersAndTransactionHeadDTO body = customerService.getCustomersAndTransactionHead(transactionHeadId);
        return ResponseEntity.ok(body);
    }

    /**
     * Handles requests to retrieve a customer.
     *
     * @param customerId ID of the customer.
     * @return The customer.
     */
    @GetMapping(ApiPaths.CUSTOMER)
    public ResponseEntity<CustomerDTO> getCustomer(@PathVariable final int customerId) {
        logger.info("Request to get customer with ID: {} received.", customerId);

        CustomerDTO body = customerService.getCustomer(customerId);
        return ResponseEntity.ok(body);
    }

    /**
     * Handles requests to retrieve a customer and all related transaction heads.
     *
     * @param customerId ID of the customer.
     * @return The customer and all related transaction heads.
     */
    @GetMapping(ApiPaths.CUSTOMER_WITH_TRANSACTION_HEADS)
    public ResponseEntity<CustomerAndTransactionHeadsDTO> getCustomerAndTransactionHeads(@PathVariable final int customerId) {
        logger.info("Request to get customer and transaction heads with customerId: {} received.", customerId);

        CustomerAndTransactionHeadsDTO body = customerService.getCustomerAndTransactionHeads(customerId);
        return ResponseEntity.ok(body);
    }

    /**
     * Handles requests to retrieve a customer and transaction head.
     *
     * @param customerId        ID of the customer.
     * @param transactionHeadId ID of the transaction head.
     * @return The customer and transaction head.
     */
    @GetMapping(ApiPaths.CUSTOMER_WITH_TRANSACTION_HEAD)
    public ResponseEntity<CustomerAndTransactionHeadDTO> getCustomerAndTransactionHead(
            @PathVariable final int customerId, @PathVariable final int transactionHeadId) {
        logger.info("Request to get customer with ID: {} and transaction head with ID: {} received.", customerId, transactionHeadId);

        CustomerAndTransactionHeadDTO body = customerService.getCustomerAndTransactionHead(customerId, transactionHeadId);
        return ResponseEntity.ok(body);
    }

    /**
     * Handles requests to retrieve a customer, transaction head and all transaction rows related to the transaction head.
     *
     * @param customerId        ID of the customer.
     * @param transactionHeadId ID of the transaction head.
     * @return The customer, transaction head and all transaction rows related to the transaction head.
     */
    @GetMapping(ApiPaths.CUSTOMER_WITH_TRANSACTION_HEAD_AND_ROWS)
    public ResponseEntity<CustomerWithTransactionHeadAndRowsDTO> getCustomerAndTransactionHeadAndRows(
            @PathVariable final int customerId, @PathVariable final int transactionHeadId) {
        logger.info("Request to get customer, transaction head, and rows for customerId: {} and transactionHeadId: {} received.", customerId, transactionHeadId);

        CustomerWithTransactionHeadAndRowsDTO body = customerService.getCustomerTransactionHeadAndRows(customerId, transactionHeadId);
        return ResponseEntity.ok(body);
    }

    /**
     * Handles requests to retrieve a customer, transaction head and transaction row.
     *
     * @param customerId        ID of the customer.
     * @param transactionHeadId ID of the transaction head.
     * @param transactionRowId  ID of the transaction row.
     * @return The customer, transaction head and transaction row.
     */
    @GetMapping(ApiPaths.CUSTOMER_WITH_TRANSACTION_HEAD_AND_ROW)
    public ResponseEntity<?> getCustomerAndTransactionHeadAndTransactionRow(@PathVariable final int customerId,
                                                                            @PathVariable final int transactionHeadId, @PathVariable final int transactionRowId) {
        logger.info("Request to get customer, transaction head, and row for customerId: {}, transactionHeadId: {}, and transactionRowId: {} received.", customerId, transactionHeadId, transactionRowId);

        CustomerWithTransactionHeadAndRowDTO body = customerService.getCustomerAndTransactionHeadAndTransactionRow(customerId, transactionHeadId, transactionRowId);
        return ResponseEntity.ok(body);
    }
}
