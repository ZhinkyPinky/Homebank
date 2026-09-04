package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.AccountRecoveryService;
import com.example.Homebank.presentation.dto.auth.AccessAndRefreshTokenDTO;
import com.example.Homebank.presentation.dto.auth.EmailDTO;
import com.example.Homebank.presentation.dto.auth.SetNewPasswordDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @Test
    void setNewPassword_validRequest_setsAuthScopedRefreshCookie() throws Exception {
        SetNewPasswordDTO request = new SetNewPasswordDTO("recovery-token", "new-secret", "new-secret");
        when(accountRecoveryService.setNewPassword(request)).thenReturn(new AccessAndRefreshTokenDTO(
                "access-token",
                "refresh-token",
                Duration.ofDays(7),
                "Password changed successfully",
                "ACTIVE"
        ));

        String json = """
                {
                  "recoveryToken": "recovery-token",
                  "newPassword": "new-secret",
                  "confirmNewPassword": "new-secret"
                }
                """;

        mockMvc.perform(post("/account-recovery/set-new-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/auth;")));

        verify(accountRecoveryService).setNewPassword(request);
    }
}
