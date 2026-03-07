package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.security.AuthenticatedUserProvider;
import com.example.Homebank.dataAccess.entities.CustomerEntity;
import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.repositories.CustomerViewRepository;
import com.example.Homebank.dataAccess.views.CustomerView;
import com.example.Homebank.dataAccess.repositories.CustomerRepository;
import com.example.Homebank.presentation.dto.composite.*;
import com.example.Homebank.presentation.dto.customer.*;
import com.example.Homebank.presentation.dto.transactionhead.TransactionHeadDTO;
import com.example.Homebank.presentation.dto.transactionhead.TransactionRowDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for handling operations related to customers.
 */
@Service
@RequiredArgsConstructor
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final AuthenticatedUserProvider authenticatedUserProvider;

    private final CustomerRepository customerRepository;
    private final CustomerViewRepository customerViewRepository;
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
        UserEntity authenticatedUser = authenticatedUserProvider.getAuthenticatedUser();
        List<Integer> accessibleCustomerIds = customerRepository.findAccessibleCustomerIds(authenticatedUser.getId());

        if (accessibleCustomerIds.isEmpty()) {
            logger.debug("No accessible customers found for authenticated user with ID: {}.", authenticatedUser.getId());
            return List.of();
        }

        List<CustomerDTO> customers = customerViewRepository.findAllById(accessibleCustomerIds).stream().map(CustomerDTO::fromEntity).toList();

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

        Set<Integer> accessibleCustomerIds = new HashSet<>();
        for (CustomerDTO customer : customers) {
            accessibleCustomerIds.add(customer.id());
        }

        if (!accessibleCustomerIds.contains(transactionHead.lenderId()) && !accessibleCustomerIds.contains(transactionHead.borrowerId())) {
            logger.warn("Access denied to transactionHeadId={} for authenticated user.", transactionHeadId);
            throw new AccessDeniedException("You do not have permission to access this transaction head.");
        }

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

        UserEntity authenticatedUser = authenticatedUserProvider.getAuthenticatedUser();
        CustomerEntity customerEntity = getCustomerEntity(customerId);
        validateCustomerAccess(customerEntity, authenticatedUser);

        CustomerView customerView = customerViewRepository.findById(customerId).orElseThrow(() -> {
            logger.error("Customer with ID: {} not found.", customerId);
            return new EntityNotFoundException("The customer could not be found.");
        });

        logger.debug("Retrieved customer: {}", customerView);
        return CustomerDTO.fromEntity(customerView);
    }

    /**
     * Validates that the authenticated user has access to the specified customer.
     *
     * @param customerEntity    The customer entity to validate access for.
     * @param authenticatedUser The currently authenticated user.
     * @throws AccessDeniedException if the user does not have access to the customer.
     */
    private void validateCustomerAccess(CustomerEntity customerEntity, UserEntity authenticatedUser) {
        int authenticatedUserId = authenticatedUser.getId();
        int customerId = customerEntity.getId();
        boolean isOwner = customerEntity.getOwner() != null && customerEntity.getOwner().getId() == authenticatedUserId;
        boolean isMember = customerEntity.getUserCustomers() != null
                && customerEntity.getUserCustomers().stream().anyMatch(link ->
                link.getUser() != null && link.getUser().getId() == authenticatedUserId
        );

        logger.debug(
                "Customer access validation for userId={} and customerId={}: isOwner={}, isMember={}",
                authenticatedUserId, customerId, isOwner, isMember
        );

        if (!isOwner && !isMember) {
            logger.warn("Customer access denied for userId={} to customerId={}.", authenticatedUserId, customerId);
            throw new AccessDeniedException("You do not have permission to access this customer.");
        }

        logger.debug("Customer access granted for userId={} to customerId={}.", authenticatedUserId, customerId);
    }

    private CustomerEntity getCustomerEntity(int customerId) {
        logger.info("Fetching customer entity with ID: {}", customerId);

        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(() -> {
            logger.error("Customer with ID: {} not found.", customerId);
            return new EntityNotFoundException("The customer could not be found.");
        });

        logger.debug("Retrieved customer entity: {}", customerEntity);
        return customerEntity;
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
        // TODO(security): Require ownership/membership authorization for this customer read.

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
        // TODO(security): Validate authenticated user access to both customerId and transactionHeadId.

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
        // TODO(security): Validate authenticated user access to customer, head, and related rows.

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
        // TODO(security): Validate authenticated user access to customer/head/row to prevent IDOR.

        CustomerDTO customer = getCustomer(customerId);
        TransactionHeadDTO transactionHead = transactionHeadService.getTransactionHead(transactionHeadId); //TODO: Make sure that customer id match lender/borrower id?
        TransactionRowDTO transactionRow = transactionRowService.getTransactionRowById(transactionRowId);

        logger.debug("Retrieved customer with ID: {}, transaction head with ID: {}, and transaction row with ID: {}", customerId, transactionHeadId, transactionRowId);
        return new CustomerWithTransactionHeadAndRowDTO(customer, transactionHead, transactionRow);
    }

    /**
     * Saves a new customer to the DB.
     *
     * @param customer Customer to save.
     */
    @Transactional
    public void createCustomer(CreateCustomerDTO customer) {
        logger.info("Creating customer: {}", customer);

        UserEntity authenticatedUser = authenticatedUserProvider.getAuthenticatedUser();
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setOwner(authenticatedUser);
        customerEntity.setName(customer.name());

        String methodInfo = this.getClass().getSimpleName() + ": createCustomer";
        customerEntity.setRowLastEditBy(methodInfo);
        customerEntity.setRowCreatedBy(methodInfo);
        customerEntity.setDescription(customer.description());

        customerRepository.save(customerEntity);

        logger.debug("Customer created successfully.");
    }

    /**
     * Updates an existing customer in the DB.
     *
     * @param customerId ID of the customer to update.
     * @param customer   Updated customer data.
     */
    @Transactional
    public void updateCustomer(int customerId, UpdateCustomerDTO customer) {
        logger.info("Updating customer with ID: {}", customerId);

        UserEntity authenticatedUser = authenticatedUserProvider.getAuthenticatedUser();
        CustomerEntity customerEntity = getCustomerEntity(customerId);

        int ownerId = customerEntity.getOwner().getId();
        int authenticatedUserId = authenticatedUser.getId();
        if (ownerId != authenticatedUserId) {
            logger.error("User with ID: {} is not the owner of customer with ID: {}.", authenticatedUserId, customerId);
            throw new AccessDeniedException("You do not have permission to update this customer.");
        }

        LocalDateTime providedRowVersion = customer.rowVersion();
        LocalDateTime currentRowVersion = customerEntity.getRowVersion();
        if (providedRowVersion == null || currentRowVersion == null || !providedRowVersion.equals(currentRowVersion)) {
            logger.error("Row version mismatch for customer with ID: {}. Provided: {}, Current: {}", customerId, providedRowVersion, currentRowVersion);
            throw new ObjectOptimisticLockingFailureException(
                    CustomerEntity.class,
                    customerId,
                    new IllegalStateException("The customer has been modified by another process. Please refresh and try again.")
            );
        }

        customerEntity.setName(customer.name());
        customerEntity.setDescription(customer.description());

        String methodInfo = this.getClass().getSimpleName() + ": updateCustomer";
        LocalDateTime currentDateTime = LocalDateTime.now();
        customerEntity.setRowLastEditBy(methodInfo);
        customerEntity.setRowLastEditDate(currentDateTime);
        customerEntity.setRowVersion(currentDateTime);

        customerRepository.save(customerEntity);

        logger.debug("Customer with ID: {} updated successfully.", customerId);
    }
}
