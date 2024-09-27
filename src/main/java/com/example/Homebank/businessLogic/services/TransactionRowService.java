package com.example.Homebank.businessLogic.services;

import com.example.Homebank.dataAccess.entities.TransactionRow;
import com.example.Homebank.dataAccess.repositories.TransactionRowRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public void saveTransactionRow(TransactionRow transactionRow) {

    }
}
