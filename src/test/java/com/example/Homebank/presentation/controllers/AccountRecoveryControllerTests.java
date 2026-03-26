package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.AccountRecoveryService;
import com.example.Homebank.presentation.dto.auth.EmailDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountRecoveryControllerTests {

    private MockMvc mockMvc;

    @Mock
    private AccountRecoveryService accountRecoveryService;

    @InjectMocks
    private AccountRecoveryController accountRecoveryController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountRecoveryController).build();
    }

    @Test
    void initiateRecovery_validEmail_returnsOkAndCallsService() throws Exception {
        String json = """
                {
                  "email": "missing@example.com"
                }
                """;

        mockMvc.perform(post("/account-recovery/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(accountRecoveryService).initiateAccountRecovery(new EmailDTO("missing@example.com"));
    }
}
