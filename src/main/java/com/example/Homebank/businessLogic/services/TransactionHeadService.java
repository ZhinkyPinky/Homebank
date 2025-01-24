package com.example.Homebank.businessLogic.services;

import com.example.Homebank.dataAccess.entities.TransactionHeadEntity;
import com.example.Homebank.dataAccess.repositories.TransactionHeadRepository;
import com.example.Homebank.presentation.dto.TransactionHeadDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionHeadService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionHeadService.class);

    private final TransactionHeadRepository transactionHeadRepository;

    @Transactional(readOnly = true)
    public List<TransactionHeadDTO> getTransactionHeads() {
        logger.info("Fetching all transaction heads.");

        List<TransactionHeadDTO> transactionHeads = transactionHeadRepository.findAll().stream().map(TransactionHeadDTO::fromEntity).toList();

        logger.debug("Retrieved {} transaction heads.", transactionHeads.size());
        return transactionHeads;
    }

    @Transactional(readOnly = true)
    public TransactionHeadDTO getTransactionHead(long transactionHeadId) {
        logger.info("Fetching transaction head with ID: {}", transactionHeadId);

        TransactionHeadEntity transactionHeadEntity = transactionHeadRepository.findById(transactionHeadId).orElseThrow(() -> {
            logger.error("Transaction head with ID: {} not found.", transactionHeadId);
            return new EntityNotFoundException("Transaction head could not be found.");
        });

        logger.debug("Retrieved transaction head: {}", transactionHeadEntity);
        return TransactionHeadDTO.fromEntity(transactionHeadEntity);
    }

    @Transactional(readOnly = true)
    public List<TransactionHeadDTO> getTransactionHeadsByCustomerId(long customerId) {
        logger.info("Fetching all transaction heads for customer ID: {}", customerId);

        List<TransactionHeadDTO> transactionHeads = transactionHeadRepository.findAllByLenderIdOrBorrowerId(customerId).stream().map(TransactionHeadDTO::fromEntity).toList();

        logger.debug("Retrieved {} transaction heads for customer ID: {}", transactionHeads.size(), customerId);
        return transactionHeads;
    }

    @Transactional
    public Map<String, Object> saveTransactionHead(TransactionHeadDTO transactionHead) {
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
        return result;
    }

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
