package com.example.Homebank.presentation.dto;

import com.example.Homebank.dataAccess.entities.TransactionHeadEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionHeadDTO(
        long id,
        long lenderId,
        long borrowerId,
        String transactionName,
        String description,
        LocalDate startDate,
        LocalDate prelEndDate,
        LocalDate endDate,
        int amount,
        String borrower,
        String lender,
        LocalDateTime rowVersion
) {
    public static TransactionHeadDTO fromEntity(TransactionHeadEntity entity) {
        return new TransactionHeadDTO(
                entity.getId(),
                entity.getLenderId(),
                entity.getBorrowerId(),
                entity.getTransactionName(),
                entity.getDescription(),
                entity.getStartDate(),
                entity.getPrelEndDate(),
                entity.getEndDate(),
                entity.getAmount(),
                entity.getBorrower(),
                entity.getLender(),
                entity.getRowVersion()
        );
    }
}
