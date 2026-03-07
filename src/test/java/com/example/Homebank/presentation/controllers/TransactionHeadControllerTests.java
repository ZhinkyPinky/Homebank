package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.TransactionHeadService;
import com.example.Homebank.presentation.dto.transactionhead.TransactionHeadDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransactionHeadControllerTests {

    private MockMvc mockMvc;

    @Mock
    private TransactionHeadService transactionHeadService;

    @InjectMocks
    private TransactionHeadController transactionHeadController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionHeadController).build();
    }

    @Test
    void saveTransactionHead_validPayload_returnsOkAndCallsService() throws Exception {
        TransactionHeadDTO request = validTransactionHeadDto(10);

        String json = """
                {
                  "id": 10,
                  "lenderId": 1,
                  "borrowerId": 2,
                  "transactionName": "Loan",
                  "description": "Head desc",
                  "startDate": "2026-01-01",
                  "prelEndDate": null,
                  "endDate": null,
                  "amount": 100,
                  "borrower": "Borrower",
                  "lender": "Lender",
                  "rowVersion": "2026-01-01T00:00:00"
                }
                """;

        mockMvc.perform(post("/transactionHeads/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Transaction head saved"));

        verify(transactionHeadService).saveTransactionHead(request);
    }

    @Test
    void saveTransactionHead_missingTransactionName_returnsBadRequestAndDoesNotCallService() throws Exception {
        String invalidJson = """
                {
                  "id": 10,
                  "lenderId": 1,
                  "borrowerId": 2,
                  "transactionName": "",
                  "startDate": "2026-01-01"
                }
                """;

        mockMvc.perform(post("/transactionHeads/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(transactionHeadService, never()).saveTransactionHead(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deleteTransactionHead_validPayload_returnsOkAndCallsService() throws Exception {
        TransactionHeadDTO request = validTransactionHeadDto(22);

        String json = """
                {
                  "id": 22,
                  "lenderId": 1,
                  "borrowerId": 2,
                  "transactionName": "Delete Head",
                  "description": "Head desc",
                  "startDate": "2026-01-01",
                  "prelEndDate": null,
                  "endDate": null,
                  "amount": 100,
                  "borrower": "Borrower",
                  "lender": "Lender",
                  "rowVersion": "2026-01-01T00:00:00"
                }
                """;

        mockMvc.perform(post("/transactionHeads/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Transaction head deleted"));

        verify(transactionHeadService).deleteTransactionHead(request);
    }

    @Test
    void deleteTransactionHead_missingId_returnsBadRequestAndDoesNotCallService() throws Exception {
        String invalidJson = """
                {
                  "id": null,
                  "lenderId": 1,
                  "borrowerId": 2,
                  "transactionName": "Delete Head",
                  "startDate": "2026-01-01"
                }
                """;

        mockMvc.perform(post("/transactionHeads/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(transactionHeadService, never()).deleteTransactionHead(org.mockito.ArgumentMatchers.any());
    }

    private TransactionHeadDTO validTransactionHeadDto(int id) {
        return new TransactionHeadDTO(
                id,
                1,
                2,
                id == 10 ? "Loan" : "Delete Head",
                "Head desc",
                LocalDate.parse("2026-01-01"),
                null,
                null,
                100,
                "Borrower",
                "Lender",
                LocalDateTime.parse("2026-01-01T00:00:00")
        );
    }
}
