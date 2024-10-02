package com.example.Homebank.businessLogic.services;

import com.example.Homebank.dataAccess.entities.TransactionHead;
import com.example.Homebank.dataAccess.repositories.TransactionHeadRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class TransactionHeadService {
    @Autowired
    private TransactionHeadRepository transactionHeadRepository;

    public List<TransactionHead> getAll() {
        return transactionHeadRepository.findAll();
    }

    public TransactionHead getTransactionHead(long transactionHeadId) {
        return transactionHeadRepository.findById(transactionHeadId).orElseThrow(() -> new EntityNotFoundException("The transaction head could not be found."));
    }

    public List<TransactionHead> getTransactionHeadsByCustomerId(long customerId) {
        return transactionHeadRepository.findAllByLenderIdOrBorrowerId(customerId);
    }

    @Transactional
    public Map<String, Object> saveTransactionHead(TransactionHead transactionHead) {
        return transactionHeadRepository.saveTransactionHead(
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
    }
}
