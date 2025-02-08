package com.example.Homebank.presentation.dto;

import com.example.Homebank.dataAccess.entities.TransactionHeadEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionHeadDTO(
        @NotNull(message = "Id is missing") long id,
        @NotNull(message = "Lender id is missing") long lenderId,
        @NotNull(message = "Borrower id is missing") long borrowerId,
        @NotBlank(message = "Transaction name is missing") String transactionName,
        String description,
        @NotNull(message = "Start date is missing") LocalDate startDate,
        LocalDate prelEndDate,
        LocalDate endDate,
        int amount,
        String borrower,
        String lender,
        @NotNull(message = "Row version is missing") LocalDateTime rowVersion
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
