package com.example.Homebank.businessLogic.services;

import com.example.Homebank.dataAccess.entities.TransactionHead;
import com.example.Homebank.dataAccess.entities.TransactionRow;
import com.example.Homebank.dataAccess.repositories.TransactionHeadRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class TransactionHeadService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionHeadService.class);

    @Autowired
    private TransactionHeadRepository transactionHeadRepository;

    public List<TransactionHead> getAll() {
        logger.info("Fetching all transaction heads.");
        List<TransactionHead> transactionHeads = transactionHeadRepository.findAll();
        logger.debug("Fetched {} transaction heads.", transactionHeads.size());

        return transactionHeads;
    }

    public TransactionHead getTransactionHead(long transactionHeadId) {
        logger.info("Fetching transaction head with ID: {}", transactionHeadId);
        return transactionHeadRepository.findById(transactionHeadId).orElseThrow(() -> {
            logger.error("Transaction head with ID: {} not found.", transactionHeadId);
            return new EntityNotFoundException("Transaction head could not be found.");
        });
    }

    public List<TransactionHead> getTransactionHeadsByCustomerId(long customerId) {
        logger.info("Fetching all transaction heads for customer ID: {}", customerId);
        List<TransactionHead> transactionHeads = transactionHeadRepository.findAllByLenderIdOrBorrowerId(customerId);
        logger.debug("Fetched {} transaction heads for customer ID: {}", transactionHeads.size(), customerId);

        return transactionHeads;
    }

    public Map<String, Object> saveTransactionHead(TransactionHead transactionHead) {
        logger.info("Saving transaction head: {}", transactionHead);
        Map<String, Object> result = transactionHeadRepository.saveTransactionHead(
                transactionHead.getId(),
                transactionHead.getLenderId(),
                transactionHead.getBorrowerId(),
                transactionHead.getTransactionName(),
                transactionHead.getDescription(),
                transactionHead.getStartDate(),
                transactionHead.getPrelEndDate(),
                transactionHead.getEndDate(),
                transactionHead.getRowVersion()
        );
        logger.debug("Transaction head saved successfully with result: {}", result);

        return result;
    }

    public void deleteTransactionHead(TransactionHead transactionHead) {
        logger.info("Deleting transaction head with ID: {}", transactionHead.getId());
        try {
            transactionHeadRepository.deleteTransactionHead(
                    transactionHead.getId(),
                    transactionHead.getRowVersion()
            );
            logger.debug("Transaction head with ID: {} deleted successfully.", transactionHead.getId());
        } catch (Exception e) {
            logger.error("Failed to delete transaction head with ID: {}. Error: {}", transactionHead.getId(), e.getMessage(), e);
            throw e;
        }
    }
}
