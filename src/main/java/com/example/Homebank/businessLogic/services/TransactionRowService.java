package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.security.authorization.TransactionHeadAccessPolicy;
import com.example.Homebank.dataAccess.views.TransactionRowView;
import com.example.Homebank.dataAccess.repositories.TransactionRowViewRepository;
import com.example.Homebank.exceptions.notfound.ResourceNotFoundException;
import com.example.Homebank.presentation.dto.transactionrow.TransactionRowDTO;
import com.example.Homebank.presentation.dto.transactionrow.CreateTransactionRowDTO;
import com.example.Homebank.presentation.dto.transactionrow.UpdateTransactionRowDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for handling operations related to transaction rows.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionRowService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionRowService.class);

    private final TransactionRowViewRepository transactionRowViewRepository;
    private final TransactionHeadAccessPolicy transactionHeadAccessPolicy;

    /**
     * Retrieves all transaction rows related to the specified transaction head.
     *
     * @param transactionHeadId ID of the transaction head.
     * @return All transaction rows related to the specified transaction head.
     */
    public List<TransactionRowDTO> getAllByTransactionHeadId(int transactionHeadId) {
        logger.info("Fetching all transaction rows for transaction head ID: {}", transactionHeadId);

        transactionHeadAccessPolicy.requireAccess(transactionHeadId);

        List<TransactionRowDTO> transactionRows = transactionRowViewRepository.findAllByTransactionHeadId(transactionHeadId).stream().map(TransactionRowDTO::fromEntity).toList();

        logger.debug("Fetched {} transaction rows for transaction head ID: {}.", transactionRows.size(), transactionHeadId);
        return transactionRows;
    }

    /**
     * Retrieves the specified transaction row.
     *
     * @param transactionRowId ID of the transaction row.
     * @return The specified transaction row.
     */
    public TransactionRowDTO getTransactionRowById(int transactionRowId) {
        logger.info("Fetching transaction row with ID: {}", transactionRowId);

        TransactionRowView transactionRowView = loadAccessibleRow(transactionRowId);

        logger.debug("Retrieved transaction row: {}", transactionRowView);
        return TransactionRowDTO.fromEntity(transactionRowView);
    }

    /**
     * Creates a row under an accessible head, with a database-assigned ID and version.
     *
     * @param transactionRow The data for the new transaction row.
     */
    @Transactional
    public void createTransactionRow(CreateTransactionRowDTO transactionRow) {
        transactionHeadAccessPolicy.requireAccess(transactionRow.transactionHeadId());

        Map<String, Object> result = transactionRowViewRepository.saveTransactionRow(
                -1,
                transactionRow.transactionHeadId(),
                transactionRow.transactionRowNo(),
                transactionRow.typeOfTransactionCode(),
                transactionRow.name(),
                transactionRow.description(),
                transactionRow.paymentDate(),
                transactionRow.amount(),
                null
        );

        logger.debug("Transaction row saved successfully with result: {}", result);
    }

    /**
     * Sets the transaction row as deleted in the DB.
     *
     * @param transactionRowId The id of the transaction row to be set as deleted.
     */
    @Transactional
    public void deleteTransactionRow(int transactionRowId, LocalDateTime rowVersion) {
        logger.info("Deleting transaction row with ID: {}", transactionRowId);

        TransactionRowView transactionRowView = loadAccessibleRow(transactionRowId);

        requireMatchingVersion(transactionRowView, rowVersion);

        //TODO: Handle on backend rather than using a procedure?
        try {
            Map<String, Object> result = transactionRowViewRepository.deleteTransactionRow(
                    transactionRowView.getId(),
                    transactionRowView.getTransactionHeadId(),
                    transactionRowView.getTransactionRowNo(),
                    rowVersion
            );

            logger.debug("Transaction row with ID: {} deleted successfully with result {}.", transactionRowView.getId(), result);
        } catch (Exception e) {
            logger.error("Failed to delete transaction row with ID: {}. Error: {}", transactionRowView.getId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Updates the specified transaction row with the provided data.
     *
     * @param transactionRowId The id of the transaction row to be updated.
     * @param transactionRow   The data to update the transaction row with.
     */
    @Transactional
    public void updateTransactionRow(int transactionRowId, UpdateTransactionRowDTO transactionRow) {
        TransactionRowView transactionRowView = loadAccessibleRow(transactionRowId);

        LocalDateTime providedRowVersion = transactionRow.rowVersion();
        requireMatchingVersion(transactionRowView, providedRowVersion);

        transactionRowViewRepository.saveTransactionRow(
                transactionRowId,
                transactionRowView.getTransactionHeadId(),
                transactionRow.transactionRowNo(),
                transactionRow.typeOfTransactionCode(),
                transactionRow.name(),
                transactionRow.description(),
                transactionRow.paymentDate(),
                transactionRow.amount(),
                providedRowVersion
        );

        logger.debug("Transaction row with ID: {} updated successfully.", transactionRowId);
    }

    /**
     * Checks if the provided row version matches the current version of the transaction row.
     *
     * @param row                The transaction row to check.
     * @param providedRowVersion The provided row version to compare against.
     * @throws ObjectOptimisticLockingFailureException if the versions do not match.
     */
    private void requireMatchingVersion(TransactionRowView row, LocalDateTime providedRowVersion) {
        if (providedRowVersion == null || !providedRowVersion.equals(row.getRowVersion())) {
            throw new ObjectOptimisticLockingFailureException(
                    TransactionRowView.class, row.getId(),
                    new IllegalStateException("The transaction row has been modified. Please refresh and try again.")
            );
        }
    }

    /**
     * Loads the specified transaction row and checks if the current user has access to it.
     *
     * @param transactionRowId The id of the transaction row to load.
     * @return The loaded transaction row.
     * @throws ResourceNotFoundException if the transaction row does not exist.
     * @throws AccessDeniedException     if the current user does not have access to the transaction row.
     */
    private TransactionRowView loadAccessibleRow(int transactionRowId) {
        TransactionRowView row = transactionRowViewRepository.findById(transactionRowId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TRANSACTION_ROW", transactionRowId, "The transaction row could not be found."
                ));
        transactionHeadAccessPolicy.requireAccess(row.getTransactionHeadId());
        return row;
    }
}
