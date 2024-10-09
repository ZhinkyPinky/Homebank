package com.example.Homebank.businessLogic.services;

import com.example.Homebank.dataAccess.entities.TransactionRow;
import com.example.Homebank.dataAccess.repositories.TransactionRowRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TransactionRowService {
    @Autowired
    private TransactionRowRepository transactionRowRepository;

    public List<TransactionRow> getAllByTransactionHeadId(long transactionHeadId) {
        return transactionRowRepository.findAllByTransactionHeadId(transactionHeadId);
    }

    public TransactionRow getTransactionRow(long transactionRowId) {
        return transactionRowRepository.findById(transactionRowId).orElseThrow(() -> new EntityNotFoundException("The transaction row could not be found."));
    }

    @Transactional
    public Map<String, Object> saveTransactionRow(TransactionRow transactionRow) {
        return transactionRowRepository.saveTransactionRow(
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
    }

    public Map<String, Object> deleteTransactionRow(TransactionRow transactionRow) {
        return transactionRowRepository.deleteTransactionRow(
                transactionRow.getId(),
                transactionRow.getTransactionHeadId(),
                transactionRow.getTransactionRowNo(),
                transactionRow.getRowVersion()
        );
    }
}
