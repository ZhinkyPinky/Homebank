package com.example.Homebank.businessLogic.services;

import com.example.Homebank.dataAccess.entities.TransactionRow;
import com.example.Homebank.dataAccess.repositories.TransactionRowRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionRowService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionRowService.class);

    private final TransactionRowRepository transactionRowRepository;

    public List<TransactionRow> getAllByTransactionHeadId(long transactionHeadId) {
        logger.info("Fetching all transaction rows for transaction head ID: {}", transactionHeadId);
        List<TransactionRow> transactionRows = transactionRowRepository.findAllByTransactionHeadId(transactionHeadId);
        logger.debug("Fetched {} transaction rows for transaction head ID: {}.", transactionRows.size(), transactionHeadId);

        return transactionRows;
    }

    public TransactionRow getTransactionRow(long transactionRowId) {
        logger.info("Fetching transaction row with ID: {}", transactionRowId);
        return transactionRowRepository.findById(transactionRowId).orElseThrow(() -> {
            logger.error("Transaction row with ID: {} not found.", transactionRowId);
            return new EntityNotFoundException("The transaction row could not be found.");
        });
    }

    public Map<String, Object> saveTransactionRow(TransactionRow transactionRow) {
        logger.info("Saving transaction row: {}", transactionRow);
        Map<String, Object> result = transactionRowRepository.saveTransactionRow(
                transactionRow.getId(),
                transactionRow.getTransactionHeadId(),
                transactionRow.getTransactionRowNo(),
                transactionRow.getTypeOfTransactionCode(),
                transactionRow.getName(),
                transactionRow.getDescription(),
                transactionRow.getPaymentDate(),
                transactionRow.getAmount(),
                transactionRow.getRowVersion()
        );
        logger.debug("Transaction row saved successfully with result: {}", result);

        return result;
    }

    public Map<String, Object> deleteTransactionRow(TransactionRow transactionRow) {
        logger.info("Deleting transaction row with ID: {}", transactionRow.getId());
        try {
            Map<String, Object> result = transactionRowRepository.deleteTransactionRow(
                    transactionRow.getId(),
                    transactionRow.getTransactionHeadId(),
                    transactionRow.getTransactionRowNo(),
                    transactionRow.getRowVersion()
            );
            logger.debug("Transaction row with ID: {} deleted successfully with result {}.", transactionRow.getId(), result);
            return result;
        } catch (Exception e) {
            logger.error("Failed to delete transaction row with ID: {}. Error: {}", transactionRow.getId(), e.getMessage(), e);
            throw e;
        }
    }
}
