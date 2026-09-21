package com.example.Homebank.presentation.dto.transactionhead;

import com.example.Homebank.dataAccess.views.TransactionHeadView;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Transaction-head data used in request/response payloads.
 *
 * @param id              Transaction head identifier.
 * @param lenderId        Lender customer identifier.
 * @param borrowerId      Borrower customer identifier.
 * @param transactionName Transaction name.
 * @param description     Optional description.
 * @param startDate       Transaction start date.
 * @param prelEndDate     Optional preliminary end date.
 * @param endDate         Optional final end date.
 * @param amount          Optional aggregated amount.
 * @param borrower        Optional borrower display name.
 * @param lender          Optional lender display name.
 * @param rowVersion      Concurrency/version timestamp.
 */
public record TransactionHeadDTO(
        @NotNull(message = "Id is missing") Integer id,
        @NotNull(message = "Lender id is missing") Integer lenderId,
        @NotNull(message = "Borrower id is missing") Integer borrowerId,
        @NotBlank(message = "Transaction name is missing") String transactionName,
        String description,
        @NotNull(message = "Start date is missing") LocalDate startDate,
        LocalDate prelEndDate,
        LocalDate endDate,
        Integer amount,
        String borrower,
        String lender,
        LocalDateTime rowVersion
) {
    public static TransactionHeadDTO fromView(TransactionHeadView view) {
        return new TransactionHeadDTO(
                view.getId(),
                view.getLenderId(),
                view.getBorrowerId(),
                view.getTransactionName(),
                view.getDescription(),
                view.getStartDate(),
                view.getPrelEndDate(),
                view.getEndDate(),
                view.getAmount(),
                view.getBorrower(),
                view.getLender(),
                view.getRowVersion()
        );
    }
}
