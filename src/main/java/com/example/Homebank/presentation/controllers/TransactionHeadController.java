package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.TransactionHeadService;
import com.example.Homebank.presentation.ApiPaths;
import com.example.Homebank.presentation.dto.TransactionHeadDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.TRANSACTION_HEADS)
public class TransactionHeadController {
    private static final Logger logger = LoggerFactory.getLogger(TransactionHeadController.class);

    private final TransactionHeadService transactionHeadService;

    /**
     * Handles requests to save a transaction head.
     *
     * @param transactionHead The transaction head to save.
     * @return Response indicating whether the transaction head was successfully saved.
     */
    @PostMapping(ApiPaths.SAVE)
    public ResponseEntity<String> saveTransactionHead(@Valid @RequestBody TransactionHeadDTO transactionHead) {
        logger.info("Request to save transaction head received.");

        transactionHeadService.saveTransactionHead(transactionHead);
        return ResponseEntity.ok("Transaction head saved");
    }

    /**
     * Handles requests to set a transaction head as deleted.
     *
     * @param transactionHead The transaction head to set as deleted.
     * @return Response indicating whether the transaction head was successfully set as deleted.
     */
    @PostMapping(ApiPaths.DELETE)
    public ResponseEntity<String> deleteTransactionHead(@Valid @RequestBody TransactionHeadDTO transactionHead) {
        logger.info("Request to delete transaction head with ID: {} received.", transactionHead.id());

        transactionHeadService.deleteTransactionHead(transactionHead);
        return ResponseEntity.ok("Transaction head deleted");
    }
}
