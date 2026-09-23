package com.example.Homebank.presentation.dto.transactionrow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

/**
 * Contains the fields required to create a new transaction row along with optional fields.
 *
 * @param transactionHeadId     Parent transaction head identifier.
 * @param transactionRowNo      Transaction row number within a head.
 * @param typeOfTransactionCode Transaction type code.
 * @param name                  Transaction row name.
 * @param description           Optional description.
 * @param paymentDate           Payment date.
 * @param amount                Monetary amount.
 */
public record CreateTransactionRowDTO(
        @NotNull(message = "Transaction head id is missing") Integer transactionHeadId,
        @NotNull(message = "Transaction row number is missing") Integer transactionRowNo,
        @NotBlank(message = "Transaction code is missing") String typeOfTransactionCode,
        @NotBlank(message = "Name is missing") String name,
        String description,
        @NotNull(message = "Payment date is missing") LocalDate paymentDate,
        @NotNull(message = "Amount is missing")
        @PositiveOrZero(message = "Amount must be zero or positive") Integer amount
) {
}
