package com.example.Homebank.presentation.dto;

import com.example.Homebank.dataAccess.entities.TransactionRowEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionRowDTO(
        long id,
        long transactionHeadId,
        int transactionRowNo,
        String typeOfTransactionCode,
        String name,
        String description,
        LocalDate paymentDate,
        int amount,
        String transactionName,
        String typeOfTransaction,
        LocalDateTime rowVersion
) {
    public static TransactionRowDTO fromEntity(TransactionRowEntity entity) {
        return new TransactionRowDTO(
                entity.getId(),
                entity.getTransactionHeadId(),
                entity.getTransactionRowNo(),
                entity.getTypeOfTransactionCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getPaymentDate(),
                entity.getAmount(),
                entity.getTransactionName(),
                entity.getTypeOfTransaction(),
                entity.getRowVersion()
        );
    }
}
