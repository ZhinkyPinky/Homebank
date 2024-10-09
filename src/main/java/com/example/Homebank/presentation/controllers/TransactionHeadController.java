package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.TransactionHeadService;
import com.example.Homebank.dataAccess.entities.TransactionHead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/transactionHeads")
public class TransactionHeadController {
    @Autowired
    private TransactionHeadService transactionHeadService;

    @PostMapping
    public ResponseEntity<String> saveTransactionHead(@RequestParam String action, @RequestBody TransactionHead transactionHead) {
        switch (action) {
            case "save" -> transactionHeadService.saveTransactionHead(transactionHead);
            case "delete" -> transactionHeadService.deleteTransactionHead(transactionHead);
            default -> {
                return ResponseEntity.badRequest().build();
            }
        }

        return ResponseEntity.ok().build();
    }
}
