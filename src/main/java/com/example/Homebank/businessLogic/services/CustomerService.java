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

@Service
@RequiredArgsConstructor
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final TransactionHeadService transactionHeadService;
    private final TransactionRowService transactionRowService;

    @Transactional(readOnly = true)
    public List<CustomerDTO> getCustomers() {
        logger.info("Fetching all customers.");

        List<CustomerDTO> customers = customerRepository.findAll().stream().map(CustomerDTO::fromEntity).toList();

        logger.debug("Retrieved {} customers.", customers.size());
        return customers;
    }

    @Transactional(readOnly = true)
    public CustomersAndTransactionHeadDTO getCustomersAndTransactionHead(long transactionHeadId) {
        logger.info("Fetching customers and transactionHead for transactionHeadId: {}", transactionHeadId);

        List<CustomerDTO> customers = getCustomers();
        TransactionHeadDTO transactionHead = transactionHeadService.getTransactionHead(transactionHeadId);

        logger.debug("Retrieved {} customers and transaction head with ID: {}", customers.size(), transactionHeadId);
        return new CustomersAndTransactionHeadDTO(customers, transactionHead);
    }

    @Transactional(readOnly = true)
    public CustomerDTO getCustomer(long customerId) {
        logger.info("Fetching customer with ID: {}", customerId);

        CustomerView customerView = customerRepository.findById(customerId).orElseThrow(() -> {
            logger.error("Customer with ID: {} not found.", customerId);
            return new EntityNotFoundException("The customer could not be found.");
        });

        logger.debug("Retrieved customer: {}", customerView);
        return CustomerDTO.fromEntity(customerView);
    }

    @Transactional(readOnly = true)
    public CustomerAndTransactionHeadsDTO getCustomerAndTransactionHeads(long customerId) {
        logger.info("Fetching customer and transaction heads for customerId: {}", customerId);

        CustomerDTO customer = getCustomer(customerId);
        List<TransactionHeadDTO> transactionHeads = transactionHeadService.getTransactionHeadsByCustomerId(customerId);

        logger.debug("Retrieved customer with ID: {} and {} transaction heads.", customerId, transactionHeads.size());
        return new CustomerAndTransactionHeadsDTO(customer, transactionHeads);
    }

    @Transactional(readOnly = true)
    public CustomerAndTransactionHeadDTO getCustomerAndTransactionHead(long customerId, long transactionHeadId) {
        logger.info("Fetching customer and transaction head for customerId: {} and transactionHeadId: {}", customerId, transactionHeadId);

        CustomerDTO customer = getCustomer(customerId);
        TransactionHeadDTO transactionHead = transactionHeadService.getTransactionHead(transactionHeadId); //TODO: Make sure that customer id match lender/borrower id?

        logger.debug("Retrieved customer with ID: {} and transaction head with ID: {}", customerId, transactionHead);
        return new CustomerAndTransactionHeadDTO(customer, transactionHead);
    }

    @Transactional(readOnly = true)
    public CustomerWithTransactionHeadAndRowsDTO getCustomerTransactionHeadAndRows(long customerId, long transactionHeadId) {
        logger.info("Fetching customer, transaction head, and rows for customerId: {} and transactionHeadId: {}", customerId, transactionHeadId);

        CustomerDTO customer = getCustomer(customerId);
        TransactionHeadDTO transactionHead = transactionHeadService.getTransactionHead(transactionHeadId); //TODO: Make sure that customer id match lender/borrower id?
        List<TransactionRowDTO> transactionRows = transactionRowService.getAllByTransactionHeadId(transactionHeadId);

        logger.debug("Retrieved customer with ID: {}, transaction head with ID: {}, and {} transaction rows", customerId, transactionHeadId, transactionRows.size());
        return new CustomerWithTransactionHeadAndRowsDTO(customer, transactionHead, transactionRows);
    }

    @Transactional(readOnly = true)
    public CustomerWithTransactionHeadAndRowDTO getCustomerAndTransactionHeadAndTransactionRow(long customerId, long transactionHeadId, long transactionRowId) {
        logger.info("Fetching customer, transaction head, and row for customerId: {}, transactionHeadId: {}, and transactionRowId: {}", customerId, transactionHeadId, transactionRowId);

        CustomerDTO customer = getCustomer(customerId);
        TransactionHeadDTO transactionHead = transactionHeadService.getTransactionHead(transactionHeadId); //TODO: Make sure that customer id match lender/borrower id?
        TransactionRowDTO transactionRow = transactionRowService.getTransactionRowById(transactionRowId);

        logger.debug("Retrieved customer with ID: {}, transaction head with ID: {}, and transaction row with ID: {}", customerId, transactionHeadId, transactionRowId);
        return new CustomerWithTransactionHeadAndRowDTO(customer, transactionHead, transactionRow);
    }
}