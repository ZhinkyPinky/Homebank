package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.TransactionRowService;
import com.example.Homebank.dataAccess.entities.TransactionRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/transactionRow")
public class TransactionRowController {
    @Autowired
    private TransactionRowService transactionRowService;

    @GetMapping("/{transactionRowId}")
    public ResponseEntity<?> getTransactionRow(@PathVariable long transactionRowId) {
        TransactionRow transactionRow = transactionRowService.getTransactionRow(transactionRowId);

        return ResponseEntity.ok(transactionRow);
    }

    @PostMapping
    public ResponseEntity<String> saveTransactionRow(@RequestBody TransactionRow transactionRow) {
        transactionRowService.saveTransactionRow(transactionRow);

        return ResponseEntity.ok().build();
    }
}
