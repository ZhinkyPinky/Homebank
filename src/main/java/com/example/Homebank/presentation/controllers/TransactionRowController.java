package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.TransactionRowService;
import com.example.Homebank.presentation.ApiPaths;
import com.example.Homebank.presentation.dto.transactionrow.TransactionRowDTO;
import com.example.Homebank.presentation.dto.transactionrow.CreateTransactionRowDTO;
import com.example.Homebank.presentation.dto.transactionrow.UpdateTransactionRowDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controller responsible for handling requests related to transaction rows, such as retrieving, saving and deleting transaction rows.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.TRANSACTION_ROWS)
public class TransactionRowController {
    private static final Logger logger = LoggerFactory.getLogger(TransactionRowController.class);

    private final TransactionRowService transactionRowService;

    /**
     * Handles requests to retrieve a transaction row.
     *
     * @param transactionRowId ID of the transaction row.
     * @return The transaction row.
     */
    @GetMapping(ApiPaths.TRANSACTION_ROW)
    public ResponseEntity<TransactionRowDTO> get(@PathVariable final int transactionRowId) {
        logger.info("Request to get transaction row with ID: {} received.", transactionRowId);

        TransactionRowDTO transactionRow = transactionRowService.getTransactionRowById(transactionRowId);
        return ResponseEntity.ok(transactionRow);
    }

    /**
     * Handles requests to save a transaction row.
     *
     * @param transactionRow The transaction row to save.
     * @return Response indicating whether the transaction row was successfully saved.
     */
    @PostMapping
    public ResponseEntity<String> postTransactionRow(@Valid @RequestBody final CreateTransactionRowDTO transactionRow) {
        logger.info("Request to create transaction row received.");

        transactionRowService.createTransactionRow(transactionRow);
        return ResponseEntity.ok("Transaction row created.");
    }

    /**
     * Handles requests to update a transaction row.
     *
     * @param transactionRowId The id of the transaction row to be updated.
     * @param transactionRow   The updated transaction row data.
     * @return Response indicating whether the transaction row was successfully updated.
     */
    @PutMapping(ApiPaths.TRANSACTION_ROW)
    public ResponseEntity<String> updateTransactionRow(
            @PathVariable int transactionRowId,
            @Valid @RequestBody final UpdateTransactionRowDTO transactionRow
    ) {
        logger.info("Request to update transaction row with ID: {} received.", transactionRowId);

        transactionRowService.updateTransactionRow(transactionRowId, transactionRow);
        return ResponseEntity.ok("Transaction row updated.");
    }


    /**
     * Handles requests to set a transaction row as deleted.
     *
     * @param transactionRowId The id of the transaction row to be set as deleted.
     * @param rowVersion       The version of the transaction row to ensure concurrency control.
     * @return Response indicating whether the transaction row was successfully set as deleted.
     */
    @DeleteMapping(ApiPaths.TRANSACTION_ROW)
    public ResponseEntity<Void> deleteTransactionRow(
            @PathVariable int transactionRowId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime rowVersion
    ) {
        logger.info("Request to delete transaction row with ID: {} received.", transactionRowId);

        transactionRowService.deleteTransactionRow(transactionRowId, rowVersion);
        return ResponseEntity.ok().build();
    }
}
