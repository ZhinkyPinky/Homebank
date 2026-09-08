package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.security.authorization.CustomerAccessPolicy;
import com.example.Homebank.businessLogic.security.authorization.TransactionHeadAccessPolicy;
import com.example.Homebank.dataAccess.views.TransactionHeadView;
import com.example.Homebank.dataAccess.repositories.TransactionHeadRepository;
import com.example.Homebank.exceptions.authorization.ResourceAccessDeniedException;
import com.example.Homebank.exceptions.notfound.ResourceNotFoundException;
import com.example.Homebank.presentation.dto.transactionhead.TransactionHeadDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Service for managing transaction heads.
 */
@Service
@RequiredArgsConstructor
public class TransactionHeadService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionHeadService.class);

    private final CustomerAccessPolicy customerAccessPolicy;
    private final TransactionHeadAccessPolicy transactionHeadAccessPolicy;
    private final TransactionHeadRepository transactionHeadRepository;

    /**
     * Retrieves the specified transaction head.
     *
     * @param transactionHeadId ID of the transaction head.
     * @return The specified transaction head.
     * @throws ResourceNotFoundException     if the transaction head does not exist.
     * @throws ResourceAccessDeniedException if neither its lender nor borrower is accessible to the authenticated user.
     */
    @Transactional(readOnly = true)
    public TransactionHeadDTO getTransactionHead(int transactionHeadId) {
        logger.info("Fetching transaction head with ID: {}", transactionHeadId);

        TransactionHeadDTO transactionHead = loadTransactionHead(transactionHeadId);
        transactionHeadAccessPolicy.requireReadAccess(transactionHead);

        logger.debug("Retrieved transaction head: {}", transactionHead);
        return transactionHead;
    }

    /**
     * Retrieves a transaction head in the context of a specified customer.
     *
     * @param customerId        ID of the customer that must be linked to the transaction head.
     * @param transactionHeadId ID of the transaction head.
     * @return The specified transaction head.
     * @throws ResourceAccessDeniedException if the customer is inaccessible or the transaction head is not linked to it.
     * @throws ResourceNotFoundException     if the transaction head does not exist.
     */
    @Transactional(readOnly = true)
    public TransactionHeadDTO getTransactionHeadForCustomer(int customerId, int transactionHeadId) {
        logger.info("Fetching transaction head with ID: {} for customer ID: {}", transactionHeadId, customerId);

        customerAccessPolicy.requireReadAccess(customerId);
        return getTransactionHeadForAccessibleCustomer(customerId, transactionHeadId);
    }

    /**
     * Loads a transaction head for a customer whose access has already been authorized by the caller.
     * The transaction head is still required to be linked to that customer.
     */
    TransactionHeadDTO getTransactionHeadForAccessibleCustomer(int customerId, int transactionHeadId) {
        TransactionHeadDTO transactionHead = loadTransactionHead(transactionHeadId);
        transactionHeadAccessPolicy.requireLinkedToCustomer(customerId, transactionHead);

        return transactionHead;
    }

    /**
     * Retrieves all transaction heads related to the specified customer.
     *
     * @param customerId ID of the customer.
     * @return All transaction heads related to the specified customer.
     * @throws ResourceAccessDeniedException if the customer is not accessible to the authenticated user.
     */
    @Transactional(readOnly = true)
    public List<TransactionHeadDTO> getTransactionHeadsByCustomerId(int customerId) {
        logger.info("Fetching all transaction heads for customer ID: {}", customerId);

        customerAccessPolicy.requireReadAccess(customerId);
        return getTransactionHeadsForAccessibleCustomer(customerId);
    }

    /**
     * Loads transaction heads for a customer whose access has already been authorized by the caller.
     */
    List<TransactionHeadDTO> getTransactionHeadsForAccessibleCustomer(int customerId) {
        List<TransactionHeadDTO> transactionHeads = transactionHeadRepository.findAllByLenderIdOrBorrowerId(customerId).stream().map(TransactionHeadDTO::fromEntity).toList();

        logger.debug("Retrieved {} transaction heads for customer ID: {}", transactionHeads.size(), customerId);
        return transactionHeads;
    }

    /**
     * Loads and maps a transaction head without applying authorization. Callers must perform the
     * authorization appropriate to their endpoint before returning the result.
     *
     * @param transactionHeadId ID of the transaction head to load.
     * @return The mapped transaction head.
     * @throws ResourceNotFoundException if the transaction head does not exist.
     */
    private TransactionHeadDTO loadTransactionHead(int transactionHeadId) {
        TransactionHeadView transactionHeadView = transactionHeadRepository.findById(transactionHeadId).orElseThrow(() -> {
            logger.error("Transaction head with ID: {} not found.", transactionHeadId);
            return new ResourceNotFoundException("TRANSACTION_HEAD", transactionHeadId, "Transaction head could not be found.");
        });

        return TransactionHeadDTO.fromEntity(transactionHeadView);
    }


    /**
     * Saves a transaction head to the DB.
     *
     * @param transactionHead Transaction head to save.
     */
    @Transactional
    public void saveTransactionHead(TransactionHeadDTO transactionHead) {
        logger.info("Saving transaction head: {}", transactionHead);

        Map<String, Object> result = transactionHeadRepository.saveTransactionHead(
                transactionHead.id(),
                transactionHead.lenderId(),
                transactionHead.borrowerId(),
                transactionHead.transactionName(),
                transactionHead.description(),
                transactionHead.startDate(),
                transactionHead.prelEndDate(),
                transactionHead.endDate(),
                transactionHead.rowVersion()
        );

        logger.debug("Transaction head saved successfully with result: {}", result);
    }

    /**
     * Sets the transaction head as deleted in the DB.
     *
     * @param transactionHead Transaction head to set as deleted.
     */
    @Transactional
    public void deleteTransactionHead(TransactionHeadDTO transactionHead) {
        logger.info("Deleting transaction head with ID: {}", transactionHead.id());

        try {
            transactionHeadRepository.deleteTransactionHead(
                    transactionHead.id(),
                    transactionHead.rowVersion()
            );

            logger.debug("Transaction head with ID: {} deleted successfully.", transactionHead.id());
        } catch (Exception e) {
            logger.error("Failed to delete transaction head with ID: {}. Error: {}", transactionHead.id(), e.getMessage(), e);
            throw e;
        }
    }

}
