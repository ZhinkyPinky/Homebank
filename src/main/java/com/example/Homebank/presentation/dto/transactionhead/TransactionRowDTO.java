package com.example.Homebank.presentation.dto.transactionhead;

import com.example.Homebank.dataAccess.views.TransactionRowView;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Transaction-row data used in request/response payloads.
 *
 * @param id                    Transaction row identifier.
 * @param transactionHeadId     Parent transaction head identifier.
 * @param transactionRowNo      Transaction row number within a head.
 * @param typeOfTransactionCode Transaction type code.
 * @param name                  Transaction row name.
 * @param description           Optional description.
 * @param paymentDate           Payment date.
 * @param amount                Monetary amount.
 * @param transactionName       Optional parent transaction name.
 * @param typeOfTransaction     Optional transaction type display value.
 * @param rowVersion            Concurrency/version timestamp.
 */
public record TransactionRowDTO(
        @NotNull(message = "Id is missing") Integer id,
        @NotNull(message = "Transaction head id is missing") Integer transactionHeadId,
        @NotNull(message = "Transaction row number is missing") Integer transactionRowNo,
        @NotBlank(message = "Transaction code is missing") String typeOfTransactionCode,
        @NotBlank(message = "Name is missing") String name,
        String description,
        @NotNull(message = "Payment date is missing") LocalDate paymentDate,
        @NotNull(message = "Amount is missing") Integer amount,
        String transactionName,
        @NotBlank(message = "Type of transaction is missing") String typeOfTransaction,
        LocalDateTime rowVersion
) {
    public static TransactionRowDTO fromEntity(TransactionRowView entity) {
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
