package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.security.AuthenticatedUserProvider;
import com.example.Homebank.businessLogic.security.authorization.CustomerAccessPolicy;
import com.example.Homebank.dataAccess.entities.CustomerEntity;
import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.repositories.CustomerViewRepository;
import com.example.Homebank.dataAccess.views.CustomerView;
import com.example.Homebank.dataAccess.repositories.CustomerRepository;
import com.example.Homebank.exceptions.authorization.ResourceAccessDeniedException;
import com.example.Homebank.exceptions.notfound.ResourceNotFoundException;
import com.example.Homebank.presentation.dto.composite.*;
import com.example.Homebank.presentation.dto.customer.*;
import com.example.Homebank.presentation.dto.transactionhead.TransactionHeadDTO;
import com.example.Homebank.presentation.dto.transactionhead.TransactionRowDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for handling operations related to customers.
 */
@Service
@RequiredArgsConstructor
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final CustomerAccessPolicy customerAccessPolicy;

    private final CustomerRepository customerRepository;
    private final CustomerViewRepository customerViewRepository;
    private final TransactionHeadService transactionHeadService;
    private final TransactionRowService transactionRowService;

    /**
     * Retrieves all customers from the DB that the authenticated user has access to.
     *
     * @return All customers.
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> getCustomers() {
        logger.info("Fetching all customers.");
        UserEntity authenticatedUser = authenticatedUserProvider.getAuthenticatedUser();
        List<Integer> accessibleCustomerIds = customerRepository.findAccessibleCustomerIds(authenticatedUser.getId());

        if (accessibleCustomerIds.isEmpty()) {
            logger.debug("No accessible customers found for authenticated user with ID: {}.", authenticatedUser.getId());
            return List.of();
        }

        List<CustomerDTO> customers = customerViewRepository.findAllById(accessibleCustomerIds).stream().map(CustomerDTO::fromView).toList();

        logger.debug("Retrieved {} customers.", customers.size());
        return customers;
    }

    /**
     * Retrieves all customers accessible by the user and the specified transaction head.
     *
     * @param transactionHeadId ID of the transaction head to retrieve.
     * @return All customers and the specified transaction head.
     * @throws ResourceAccessDeniedException if the authenticated user does not have access to the transaction head.
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

        CustomerView customerView = customerViewRepository.findById(customerId).orElseThrow(() -> {
            logger.error("Customer with ID: {} not found.", customerId);
            return new ResourceNotFoundException("CUSTOMER", customerId, "The customer could not be found.");
        });
        customerAccessPolicy.requireReadAccess(customerId);

        logger.debug("Retrieved customer: {}", customerView);
        return CustomerDTO.fromView(customerView);
    }


    /**
     * Retrieves the CustomerEntity for the specified customer ID.
     *
     * @param customerId ID of the customer to retrieve.
     * @return The CustomerEntity for the specified customer ID.
     * @throws ResourceNotFoundException if the customer with the specified ID does not exist.
     */
    private CustomerEntity getCustomerEntity(int customerId) {
        logger.info("Fetching customer entity with ID: {}", customerId);

        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(() -> {
            logger.error("Customer with ID: {} not found.", customerId);
            return new ResourceNotFoundException("CUSTOMER", customerId, "The customer could not be found.");
        });

        logger.debug("Retrieved customer entity: {}", customerEntity);
        return customerEntity;
    }

    /**
     * Retrieves the specified customer and all transaction heads related to them.
     *
     * @param customerId ID of the customer.
     * @return The specified customer and all transaction heads related to them.
     * @throws ResourceAccessDeniedException if the authenticated user cannot access the customer.
     */
    @Transactional(readOnly = true)
    public CustomerAndTransactionHeadsDTO getCustomerAndTransactionHeads(int customerId) {
        logger.info("Fetching customer and transaction heads for customerId: {}", customerId);
        CustomerDTO customer = getCustomer(customerId);
        List<TransactionHeadDTO> transactionHeads = transactionHeadService.getTransactionHeadsForAccessibleCustomer(customerId);

        logger.debug("Retrieved customer with ID: {} and {} transaction heads.", customerId, transactionHeads.size());
        return new CustomerAndTransactionHeadsDTO(customer, transactionHeads);
    }

    /**
     * Retrieves the specified customer and transaction head.
     *
     * @param customerId        ID of the customer.
     * @param transactionHeadId ID of the transaction head.
     * @return The specified customer and transaction head.
     * @throws ResourceAccessDeniedException if the customer is inaccessible or the transaction head is not linked to it.
     */
    @Transactional(readOnly = true)
    public CustomerAndTransactionHeadDTO getCustomerAndTransactionHead(int customerId, int transactionHeadId) {
        logger.info("Fetching customer and transaction head for customerId: {} and transactionHeadId: {}", customerId, transactionHeadId);

        CustomerDTO customer = getCustomer(customerId);
        TransactionHeadDTO transactionHead = transactionHeadService.getTransactionHeadForAccessibleCustomer(customerId, transactionHeadId);

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

        CustomerAndTransactionHeadDTO customerAndTransactionHead = getCustomerAndTransactionHead(customerId, transactionHeadId);
        List<TransactionRowDTO> transactionRows = transactionRowService.getAllByTransactionHeadId(transactionHeadId);

        logger.debug("Retrieved customer with ID: {}, transaction head with ID: {}, and {} transaction rows", customerId, transactionHeadId, transactionRows.size());
        return new CustomerWithTransactionHeadAndRowsDTO(
                customerAndTransactionHead.customer(),
                customerAndTransactionHead.transactionHead(),
                transactionRows
        );
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

        CustomerAndTransactionHeadDTO customerAndTransactionHead = getCustomerAndTransactionHead(customerId, transactionHeadId);
        TransactionRowDTO transactionRow = transactionRowService.getTransactionRowById(transactionRowId);
        validateTransactionRowAccess(transactionHeadId, transactionRowId, transactionRow);

        logger.debug("Retrieved customer with ID: {}, transaction head with ID: {}, and transaction row with ID: {}", customerId, transactionHeadId, transactionRowId);
        return new CustomerWithTransactionHeadAndRowDTO(
                customerAndTransactionHead.customer(),
                customerAndTransactionHead.transactionHead(),
                transactionRow
        );
    }

    /**
     * Saves a new customer to the DB.
     *
     * @param customer Customer to save.
     */
    @Transactional
    public void createCustomer(CreateCustomerDTO customer) {
        logger.info("Creating customer: {}", customer);

        String methodInfo = this.getClass().getSimpleName() + ": createCustomer";

        UserEntity authenticatedUser = authenticatedUserProvider.getAuthenticatedUser();

        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setOwner(authenticatedUser);
        customerEntity.setName(customer.name());
        customerEntity.setRowCreatedBy(methodInfo);
        customerEntity.setRowLastEditBy(methodInfo);
        customerEntity.setDescription(customer.description());

        customerRepository.save(customerEntity);

        logger.debug("Customer created successfully.");
    }

    /**
     * Updates an existing customer in the DB.
     *
     * @param customerId ID of the customer to update.
     * @param customer   Updated customer data.
     * @throws IllegalArgumentException                if the provided row version is null.
     * @throws ObjectOptimisticLockingFailureException if the row version does not match the current version in the DB, indicating a concurrent modification.
     */
    @Transactional
    public void updateCustomer(int customerId, UpdateCustomerDTO customer) {
        logger.info("Updating customer with ID: {}", customerId);

        CustomerEntity customerEntity = getCustomerEntity(customerId);
        customerAccessPolicy.requireOwnership(
                customerEntity,
                "update",
                "You do not have permission to update this customer."
        );

        LocalDateTime providedRowVersion = customer.rowVersion();
        if (providedRowVersion == null) {
            logger.error("Provided row version is null for customer with ID: {}", customerId);
            throw new IllegalArgumentException("Row version must be provided for update operations.");
        }

        LocalDateTime currentRowVersion = customerEntity.getRowVersion();
        if (!providedRowVersion.equals(currentRowVersion)) {
            logger.error("Row version mismatch for customer with ID: {}. Provided: {}, Current: {}", customerId, providedRowVersion, currentRowVersion);
            throw new ObjectOptimisticLockingFailureException(
                    CustomerEntity.class,
                    customerId,
                    new IllegalStateException("The customer has been modified by another process. Please refresh and try again.")
            );
        }

        String methodInfo = this.getClass().getSimpleName() + ": updateCustomer";
        LocalDateTime currentDateTime = LocalDateTime.now();

        customerEntity.setName(customer.name());
        customerEntity.setDescription(customer.description());
        customerEntity.setRowLastEditBy(methodInfo);
        customerEntity.setRowLastEditDate(currentDateTime);

        customerRepository.saveAndFlush(customerEntity);

        logger.debug("Customer with ID: {} updated successfully.", customerId);
    }

    /**
     * Deletes the specified customer from the DB. Only the owner of the customer can delete it.
     *
     * @param customerId ID of the customer to delete.
     */
    @Transactional
    public void deleteCustomer(int customerId) {
        logger.info("Deleting customer with ID: {}", customerId);
        CustomerEntity customerEntity = getCustomerEntity(customerId);
        customerAccessPolicy.requireOwnership(
                customerEntity,
                "delete",
                "You do not have permission to delete this customer."
        );

        customerRepository.deleteById(customerId);
    }

    /**
     * Validates that the transaction row belongs to the specified transaction head.
     *
     * @param transactionHeadId ID of the transaction head.
     * @param transactionRowId  ID of the transaction row.
     * @param transactionRow    The transaction row to validate.
     * @throws ResourceAccessDeniedException if the transaction row does not belong to the specified transaction head.
     */
    private void validateTransactionRowAccess(int transactionHeadId, int transactionRowId, TransactionRowDTO transactionRow) {
        if (transactionRow.transactionHeadId() != transactionHeadId) {
            logger.warn(
                    "Transaction row access denied because transactionRowId={} does not belong to transactionHeadId={}.",
                    transactionRowId,
                    transactionHeadId
            );
            throw new ResourceAccessDeniedException(
                    "TRANSACTION_ROW",
                    transactionRowId,
                    "read",
                    "You do not have permission to access this transaction row for the specified transaction head.",
                    Map.of("transactionHeadId", transactionHeadId, "actualTransactionHeadId", transactionRow.transactionHeadId())
            );
        }
    }
}
