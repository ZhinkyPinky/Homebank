package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.TransactionRowService;
import com.example.Homebank.presentation.ApiPaths;
import com.example.Homebank.presentation.dto.TransactionRowDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.TRANSACTION_ROWS)
public class TransactionRowController {
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final TransactionRowService transactionRowService;

    /**
     * Handles requests to retrieve a transaction row.
     *
     * @param transactionRowId ID of the transaction row.
     * @return The transaction row.
     */
    @GetMapping(ApiPaths.TRANSACTION_ROW)
    public ResponseEntity<TransactionRowDTO> get(@PathVariable final long transactionRowId) {
        logger.info("Request to get transaction head with ID: {} received.", transactionRowId);

        TransactionRowDTO transactionRow = transactionRowService.getTransactionRowById(transactionRowId);
        return ResponseEntity.ok(transactionRow);
    }

    /**
     * Handles requests to save a transaction row.
     *
     * @param transactionRow The transaction row to save.
     * @return Response indicating whether the transaction row was successfully saved.
     */
    @PostMapping(ApiPaths.SAVE)
    public ResponseEntity<String> saveTransactionRow(@Valid @RequestBody final TransactionRowDTO transactionRow) {
        logger.info("Request to save transaction row with ID: {} received.", transactionRow.id());

        transactionRowService.saveTransactionRow(transactionRow);
        return ResponseEntity.ok().build();
    }

    /**
     * Handles requests to set a transaction row as deleted.
     *
     * @param transactionRow The transaction row to set as deleted.
     * @return Response indicating whether the transaction row was successfully set as deleted.
     */
    @PostMapping(ApiPaths.DELETE)
    public ResponseEntity<String> deleteTransactionRow(@Valid @RequestBody final TransactionRowDTO transactionRow) {
        logger.info("Request to delete transaction row with ID: {} received.", transactionRow.id());

        transactionRowService.deleteTransactionRow(transactionRow);
        return ResponseEntity.ok().build();
    }
}
