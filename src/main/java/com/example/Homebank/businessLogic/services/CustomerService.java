package com.example.Homebank.businessLogic.services;

import com.example.Homebank.dataAccess.views.CustomerView;
import com.example.Homebank.dataAccess.repositories.CustomerRepository;
import com.example.Homebank.presentation.dto.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for handling operations related to customers. Mainly used for retrieving customers and related transaction heads and rows.
 */
@Service
@RequiredArgsConstructor
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final TransactionHeadService transactionHeadService;
    private final TransactionRowService transactionRowService;

    /**
     * Retrieves all customers from the DB.
     *
     * @return All customers.
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> getCustomers() {
        logger.info("Fetching all customers.");

        List<CustomerDTO> customers = customerRepository.findAll().stream().map(CustomerDTO::fromEntity).toList();

        logger.debug("Retrieved {} customers.", customers.size());
        return customers;
    }

    /**
     * Retrieves all customers and the specified transaction head.
     *
     * @param transactionHeadId ID of the transaction head to retrieve.
     * @return All customers and the specified transaction head.
     */
    @Transactional(readOnly = true)
    public CustomersAndTransactionHeadDTO getCustomersAndTransactionHead(int transactionHeadId) {
        logger.info("Fetching customers and transactionHead for transactionHeadId: {}", transactionHeadId);

        List<CustomerDTO> customers = getCustomers();
        TransactionHeadDTO transactionHead = transactionHeadService.getTransactionHead(transactionHeadId);

        logger.debug("Retrieved {} customers and transaction head with ID: {}", customers.size(), transactionHeadId);
        return new CustomersAndTransactionHeadDTO(customers, transactionHead);
    }

    /**
     * Retrieves the specified customer.
     *
     * @param customerId ID of the customer to retrieve.
     * @return The specified customer.
     */
    @Transactional(readOnly = true)
    public CustomerDTO getCustomer(int customerId) {
        logger.info("Fetching customer with ID: {}", customerId);

        CustomerView customerView = customerRepository.findById(customerId).orElseThrow(() -> {
            logger.error("Customer with ID: {} not found.", customerId);
            return new EntityNotFoundException("The customer could not be found.");
        });

        logger.debug("Retrieved customer: {}", customerView);
        return CustomerDTO.fromEntity(customerView);
    }

    /**
     * Retrieves the specified customer and all transaction heads related to them.
     *
     * @param customerId ID of the customer.
     * @return The specified customer and all transaction heads related to them.
     */
    @Transactional(readOnly = true)
    public CustomerAndTransactionHeadsDTO getCustomerAndTransactionHeads(int customerId) {
        logger.info("Fetching customer and transaction heads for customerId: {}", customerId);

        CustomerDTO customer = getCustomer(customerId);
        List<TransactionHeadDTO> transactionHeads = transactionHeadService.getTransactionHeadsByCustomerId(customerId);

        logger.debug("Retrieved customer with ID: {} and {} transaction heads.", customerId, transactionHeads.size());
        return new CustomerAndTransactionHeadsDTO(customer, transactionHeads);
    }

    /**
     * Retrieves the specified customer and transaction head.
     *
     * @param customerId        ID of the customer.
     * @param transactionHeadId ID of the transaction head.
     * @return The specified customer and transaction head.
     */
    @Transactional(readOnly = true)
    public CustomerAndTransactionHeadDTO getCustomerAndTransactionHead(int customerId, int transactionHeadId) {
        logger.info("Fetching customer and transaction head for customerId: {} and transactionHeadId: {}", customerId, transactionHeadId);

        CustomerDTO customer = getCustomer(customerId);
        TransactionHeadDTO transactionHead = transactionHeadService.getTransactionHead(transactionHeadId); //TODO: Make sure that customer id match lender/borrower id?

        logger.debug("Retrieved customer with ID: {} and transaction head with ID: {}", customerId, transactionHead);
        return new CustomerAndTransactionHeadDTO(customer, transactionHead);
    }

    /**
     * Retrieves the specified customer, transaction head and all transaction rows related to the head.
     *
     * @param customerId        ID of the customer.
     * @param transactionHeadId ID of the transaction head.
     * @return The specified customer, transaction head and all transaction rows related to the head.
     */
    @Transactional(readOnly = true)
    public CustomerWithTransactionHeadAndRowsDTO getCustomerTransactionHeadAndRows(int customerId, int transactionHeadId) {
        logger.info("Fetching customer, transaction head, and rows for customerId: {} and transactionHeadId: {}", customerId, transactionHeadId);

        CustomerDTO customer = getCustomer(customerId);
        TransactionHeadDTO transactionHead = transactionHeadService.getTransactionHead(transactionHeadId); //TODO: Make sure that customer id match lender/borrower id?
        List<TransactionRowDTO> transactionRows = transactionRowService.getAllByTransactionHeadId(transactionHeadId);

        logger.debug("Retrieved customer with ID: {}, transaction head with ID: {}, and {} transaction rows", customerId, transactionHeadId, transactionRows.size());
        return new CustomerWithTransactionHeadAndRowsDTO(customer, transactionHead, transactionRows);
    }

    /**
     * Retrieves the specified customer, transaction head and transaction row.
     *
     * @param customerId        ID of the customer.
     * @param transactionHeadId ID of the transaction head.
     * @param transactionRowId  ID of the transaction row.
     * @return The specified customer, transaction head and transaction row.
     */
    @Transactional(readOnly = true)
    public CustomerWithTransactionHeadAndRowDTO getCustomerAndTransactionHeadAndTransactionRow(int customerId, int transactionHeadId, int transactionRowId) {
        logger.info("Fetching customer, transaction head, and row for customerId: {}, transactionHeadId: {}, and transactionRowId: {}", customerId, transactionHeadId, transactionRowId);

        CustomerDTO customer = getCustomer(customerId);
        TransactionHeadDTO transactionHead = transactionHeadService.getTransactionHead(transactionHeadId); //TODO: Make sure that customer id match lender/borrower id?
        TransactionRowDTO transactionRow = transactionRowService.getTransactionRowById(transactionRowId);

        logger.debug("Retrieved customer with ID: {}, transaction head with ID: {}, and transaction row with ID: {}", customerId, transactionHeadId, transactionRowId);
        return new CustomerWithTransactionHeadAndRowDTO(customer, transactionHead, transactionRow);
    }
}