package com.example.Homebank.businessLogic.services;

import com.example.Homebank.dataAccess.views.TransactionRowView;
import com.example.Homebank.dataAccess.repositories.TransactionRowRepository;
import com.example.Homebank.presentation.dto.TransactionRowDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for handling operations related to transaction rows.
 */
@Service
@RequiredArgsConstructor
public class TransactionRowService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionRowService.class);

    private final TransactionRowRepository transactionRowRepository;

    /**
     * Retrieves all transaction rows related to the specified transaction head.
     *
     * @param transactionHeadId ID of the transaction head.
     * @return All transaction rows related to the specified transaction head.
     */
    public List<TransactionRowDTO> getAllByTransactionHeadId(int transactionHeadId) {
        logger.info("Fetching all transaction rows for transaction head ID: {}", transactionHeadId);

        List<TransactionRowDTO> transactionRows = transactionRowRepository.findAllByTransactionHeadId(transactionHeadId).stream().map(TransactionRowDTO::fromEntity).toList();

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

        TransactionRowView transactionRowView = transactionRowRepository.findById(transactionRowId).orElseThrow(() -> {
            logger.error("Transaction row with ID: {} not found.", transactionRowId);
            return new EntityNotFoundException("The transaction row could not be found.");
        });

        logger.debug("Retrieved transaction row: {}", transactionRowView);
        return TransactionRowDTO.fromEntity(transactionRowView);
    }

    /**
     * Saves a transaction row to the DB.
     *
     * @param transactionRow The transaction row to save.
     */
    public void saveTransactionRow(TransactionRowDTO transactionRow) {
        logger.info("Saving transaction row: {}", transactionRow);

        Map<String, Object> result = transactionRowRepository.saveTransactionRow(
                transactionRow.id(),
                transactionRow.transactionHeadId(),
                transactionRow.transactionRowNo(),
                transactionRow.typeOfTransactionCode(),
                transactionRow.name(),
                transactionRow.description(),
                transactionRow.paymentDate(),
                transactionRow.amount(),
                transactionRow.rowVersion()
        );

        logger.debug("Transaction row saved successfully with result: {}", result);
    }

    /**
     * Sets the transaction row as deleted in the DB.
     *
     * @param transactionRow The transaction row to set as deleted.
     */
    public void deleteTransactionRow(TransactionRowDTO transactionRow) {
        logger.info("Deleting transaction row with ID: {}", transactionRow.id());

        try {
            Map<String, Object> result = transactionRowRepository.deleteTransactionRow(
                    transactionRow.id(),
                    transactionRow.transactionHeadId(),
                    transactionRow.transactionRowNo(),
                    transactionRow.rowVersion()
            );

            logger.debug("Transaction row with ID: {} deleted successfully with result {}.", transactionRow.id(), result);
        } catch (Exception e) {
            logger.error("Failed to delete transaction row with ID: {}. Error: {}", transactionRow.id(), e.getMessage(), e);
            throw e;
        }
    }
}
