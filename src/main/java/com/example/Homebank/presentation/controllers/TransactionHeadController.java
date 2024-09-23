package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.TransactionHeadService;
import com.example.Homebank.dataAccess.entities.TransactionHead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/transactionHead")
public class TransactionHeadController {
    @Autowired
    private TransactionHeadService transactionHeadService;

    @PostMapping
    public ResponseEntity<String> saveTransactionHead(@RequestBody TransactionHead transactionHead){
        transactionHeadService.saveTransactionHead(transactionHead);

        return ResponseEntity.ok().build();
    }
}
