package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.TransactionRowService;
import com.example.Homebank.dataAccess.entities.TransactionRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactionRows")
public class TransactionRowController {
    @Autowired
    private TransactionRowService transactionRowService;

    @GetMapping("/{transactionRowId}")
    public ResponseEntity<?> get(@PathVariable final long transactionRowId) {
        TransactionRow transactionRow = transactionRowService.getTransactionRow(transactionRowId);

        return ResponseEntity.ok(transactionRow);
    }

    @PostMapping
    public ResponseEntity<String> post(@RequestParam String action, @RequestBody final TransactionRow transactionRow) {
        switch (action) {
            case "save" -> transactionRowService.saveTransactionRow(transactionRow);
            case "delete" -> transactionRowService.deleteTransactionRow(transactionRow);
            default -> {
                return  ResponseEntity.badRequest().build();
            }
        }

        return ResponseEntity.ok().build();
    }
}
