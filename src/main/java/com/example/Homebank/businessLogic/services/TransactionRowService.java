package com.example.Homebank.businessLogic.services;

import com.example.Homebank.dataAccess.entities.TransactionRowEntity;
import com.example.Homebank.dataAccess.repositories.TransactionRowRepository;
import com.example.Homebank.presentation.dto.TransactionRowDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionRowService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionRowService.class);

    private final TransactionRowRepository transactionRowRepository;

    public List<TransactionRowDTO> getAllByTransactionHeadId(long transactionHeadId) {
        logger.info("Fetching all transaction rows for transaction head ID: {}", transactionHeadId);

        List<TransactionRowDTO> transactionRows = transactionRowRepository.findAllByTransactionHeadId(transactionHeadId).stream().map(TransactionRowDTO::fromEntity).toList();

        logger.debug("Fetched {} transaction rows for transaction head ID: {}.", transactionRows.size(), transactionHeadId);
        return transactionRows;
    }

    public TransactionRowDTO getTransactionRowById(long transactionRowId) {
        logger.info("Fetching transaction row with ID: {}", transactionRowId);

        TransactionRowEntity transactionRowEntity = transactionRowRepository.findById(transactionRowId).orElseThrow(() -> {
            logger.error("Transaction row with ID: {} not found.", transactionRowId);
            return new EntityNotFoundException("The transaction row could not be found.");
        });

        logger.debug("Retrieved transaction row: {}", transactionRowEntity);
        return TransactionRowDTO.fromEntity(transactionRowEntity);
    }

    public Map<String, Object> saveTransactionRow(TransactionRowDTO transactionRow) {
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
        return result;
    }

    public Map<String, Object> deleteTransactionRow(TransactionRowDTO transactionRow) {
        logger.info("Deleting transaction row with ID: {}", transactionRow.id());

        try {
            Map<String, Object> result = transactionRowRepository.deleteTransactionRow(
                    transactionRow.id(),
                    transactionRow.transactionHeadId(),
                    transactionRow.transactionRowNo(),
                    transactionRow.rowVersion()
            );

            logger.debug("Transaction row with ID: {} deleted successfully with result {}.", transactionRow.id(), result);
            return result;
        } catch (Exception e) {
            logger.error("Failed to delete transaction row with ID: {}. Error: {}", transactionRow.id(), e.getMessage(), e);
            throw e;
        }
    }
}
