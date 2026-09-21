package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.TransactionHeadService;
import com.example.Homebank.presentation.ApiPaths;
import com.example.Homebank.presentation.dto.transactionhead.DeleteTransactionHeadDTO;
import com.example.Homebank.presentation.dto.transactionhead.TransactionHeadDTO;
import com.example.Homebank.presentation.dto.transactionhead.CreateTransactionHeadDTO;
import com.example.Homebank.presentation.dto.transactionhead.UpdateTransactionHeadDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsible for transaction-head reads and writes.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.TRANSACTION_HEADS)
public class TransactionHeadController {
    private static final Logger logger = LoggerFactory.getLogger(TransactionHeadController.class);

    private final TransactionHeadService transactionHeadService;

    @GetMapping(ApiPaths.TRANSACTION_HEAD)
    public ResponseEntity<TransactionHeadDTO> getTransactionHead(@PathVariable int transactionHeadId) {
        logger.info("Request to get transaction head with ID: {} received.", transactionHeadId);

        TransactionHeadDTO transactionHead = transactionHeadService.getTransactionHead(transactionHeadId);
        return ResponseEntity.ok(transactionHead);
    }

    /**
     * Handles requests to create a transaction head.
     *
     * @param transactionHead The initial transaction head fields.
     * @return Response indicating whether the transaction head was successfully created.
     */
    @PostMapping
    public ResponseEntity<String> postTransactionHead(@Valid @RequestBody CreateTransactionHeadDTO transactionHead) {
        logger.info("Request to create transaction head received.");

        transactionHeadService.createTransactionHead(transactionHead);
        return ResponseEntity.ok("Transaction head created.");
    }


    /**
     * Handles requests to update a transaction head.
     *
     * @param transactionHeadId The ID of the transaction head to update.
     * @param transactionHead   The transaction head to update.
     * @return Response indicating whether the transaction head was successfully updated.
     */
    @PutMapping(ApiPaths.TRANSACTION_HEAD)
    public ResponseEntity<String> updateTransactionHead(@PathVariable int transactionHeadId, @Valid @RequestBody UpdateTransactionHeadDTO transactionHead) {
        logger.info("Request to update transaction head with ID: {} received.", transactionHeadId);

        transactionHeadService.updateTransactionHead(transactionHeadId, transactionHead);
        return ResponseEntity.ok("Transaction head updated");
    }


    /**
     * Handles requests to set a transaction head as deleted.
     *
     * @param transactionHead The transaction head to set as deleted.
     * @return Response indicating whether the transaction head was successfully set as deleted.
     */
    @PostMapping(ApiPaths.DELETE)
    public ResponseEntity<String> deleteTransactionHead(@Valid @RequestBody DeleteTransactionHeadDTO transactionHead) {
        logger.info("Request to delete transaction head with ID: {} received.", transactionHead.id());

        transactionHeadService.deleteTransactionHead(transactionHead);
        return ResponseEntity.ok("Transaction head deleted");
    }
}
