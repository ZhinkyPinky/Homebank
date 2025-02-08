package com.example.Homebank.presentation.dto;

import com.example.Homebank.dataAccess.entities.TransactionRowEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionRowDTO(
        @NotNull(message = "Id is missing") long id,
        @NotNull(message = "Transaction head id is missing") long transactionHeadId,
        @NotNull(message = "Transaction row number is missing") int transactionRowNo,
        @NotBlank(message = "Transaction code is missing") String typeOfTransactionCode,
        @NotBlank(message = "Name is missing") String name,
        String description,
        @NotNull(message = "Payment date is missing") LocalDate paymentDate,
        @NotNull(message = "Amount is missing") int amount,
        @NotBlank(message = "Transaction name is missing") String transactionName,
        @NotBlank(message = "Type of transaction is missing") String typeOfTransaction,
        @NotNull(message = "Row version is missing") LocalDateTime rowVersion
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
