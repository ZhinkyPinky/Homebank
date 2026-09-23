package com.example.Homebank.presentation.dto.transactionrow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Contains the editable fields of a transaction row.
 *
 * @param transactionRowNo      Transaction row number, used for ordering.
 * @param typeOfTransactionCode Transaction type code.
 * @param name                  Transaction row name.
 * @param description           Optional description.
 * @param paymentDate           Payment date.
 * @param amount                Monetary amount.
 * @param rowVersion            Concurrency/version timestamp.
 */
public record UpdateTransactionRowDTO(
        @NotNull(message = "Transaction row number is missing") Integer transactionRowNo,
        @NotBlank(message = "Transaction code is missing") String typeOfTransactionCode,
        @NotBlank(message = "Name is missing") String name,
        String description,
        @NotNull(message = "Payment date is missing") LocalDate paymentDate,
        @NotNull(message = "Amount is missing")
        @PositiveOrZero(message = "Amount must be zero or positive") Integer amount,
        @NotNull(message = "Row version is missing") LocalDateTime rowVersion
) {
}
