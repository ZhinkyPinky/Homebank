package com.example.Homebank.presentation.dto.transactionrow;

import com.example.Homebank.dataAccess.views.TransactionRowView;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Transaction-row response data.
 *
 * @param id                    Transaction row identifier.
 * @param transactionHeadId     Parent transaction head identifier.
 * @param transactionRowNo      Transaction row number within a head.
 * @param typeOfTransactionCode Transaction type code.
 * @param name                  Transaction row name.
 * @param description           Optional description.
 * @param paymentDate           Payment date.
 * @param amount                Monetary amount.
 * @param rowVersion            Concurrency/version timestamp.
 */
public record TransactionRowDTO(
        Integer id,
        Integer transactionHeadId,
        Integer transactionRowNo,
        String typeOfTransactionCode,
        String name,
        String description,
        LocalDate paymentDate,
        Integer amount,
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
                entity.getRowVersion()
        );
    }
}
