package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.AuthService;
import com.example.Homebank.exceptions.authentication.AccountNotActivatedException;
import com.example.Homebank.presentation.dto.auth.AccessAndRefreshTokenDTO;
import com.example.Homebank.presentation.dto.auth.AuthenticationDTO;
import com.example.Homebank.presentation.dto.auth.RefreshTokenDTO;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTests {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void login_activeAccount_returnsAccessTokenAndSecureRefreshCookie() throws Exception {
        AuthenticationDTO request = new AuthenticationDTO("user@example.com", "secret");
        when(authService.authenticate(request)).thenReturn(new AccessAndRefreshTokenDTO(
                "access-token",
                "refresh-token",
                Duration.ofDays(7),
                "Login successful",
                "ACTIVE"
        ));

        String json = """
                {
                  "email": "user@example.com",
                  "password": "secret"
                }
                """;

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=refresh-token")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, not(containsString("access-token"))))
                .andReturn();

        assertRefreshCookie(result, "refresh-token", "Max-Age=604800");
        verify(authService).authenticate(request);
    }

    @Test
    void login_pendingAccount_returnsAccessTokenAndDeletesExistingRefreshCookie() throws Exception {
        AuthenticationDTO request = new AuthenticationDTO("pending@example.com", "secret");
        when(authService.authenticate(request)).thenReturn(new AccessAndRefreshTokenDTO(
                "pending-access-token", null, null, "Login successful", "ACTIVATION_PENDING"
        ));

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE, "old-refresh-token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"pending@example.com","password":"secret"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("pending-access-token"))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVATION_PENDING"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();

        assertRefreshCookie(result, "", "Max-Age=0");
        verify(authService).authenticate(request);
    }

    @Test
    void login_invalidCredentialsPayload_returnsBadRequestWithoutCallingService() throws Exception {
        String json = """
                {
                  "email": "not-an-email",
                  "password": ""
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(authService, never()).authenticate(any());
    }

    @Test
    void login_badCredentials_returnsUnauthorizedWithoutSettingRefreshCookie() throws Exception {
        AuthenticationDTO request = new AuthenticationDTO("user@example.com", "wrong-password");
        when(authService.authenticate(request)).thenThrow(new BadCredentialsException("Bad credentials"));

        String json = """
                {
                  "email": "user@example.com",
                  "password": "wrong-password"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));

        verify(authService).authenticate(request);
    }

    @Test
    void logout_refreshCookie_revokesTokenAndDeletesCookie() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/logout")
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE, "refresh-token")))
                .andExpect(status().isNoContent())
                .andReturn();

        assertRefreshCookie(result, "", "Max-Age=0");
        verify(authService).signOut("refresh-token");
    }

    @Test
    void logout_missingRefreshCookie_stillDeletesCookieWithoutCallingService() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isNoContent())
                .andReturn();

        assertRefreshCookie(result, "", "Max-Age=0");
        verify(authService, never()).signOut(anyString());
    }

    @Test
    void logout_serviceFailure_stillDeletesCookie() throws Exception {
        doThrow(new DataAccessResourceFailureException("Database unavailable"))
                .when(authService)
                .signOut("refresh-token");

        MvcResult result = mockMvc.perform(post("/auth/logout")
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE, "refresh-token")))
                .andExpect(status().isInternalServerError())
                .andReturn();

        assertRefreshCookie(result, "", "Max-Age=0");
        verify(authService).signOut("refresh-token");
    }

    @Test
    void refresh_validCookie_returnsAccessTokenAndSecureRotatedRefreshCookie() throws Exception {
        RefreshTokenDTO request = new RefreshTokenDTO("refresh-token");
        when(authService.refreshTokens(request)).thenReturn(new AccessAndRefreshTokenDTO(
                "new-access-token",
                "new-refresh-token",
                Duration.ofDays(7),
                "Token refreshed successfully",
                "ACTIVE"
        ));

        MvcResult result = mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE, "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(jsonPath("$.accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, not(containsString("new-access-token"))))
                .andReturn();

        assertRefreshCookie(result, "new-refresh-token", "Max-Age=604800");
        verify(authService).refreshTokens(request);
    }

    @Test
    void refresh_pendingAccount_returnsForbiddenWithoutIssuingTokens() throws Exception {
        RefreshTokenDTO request = new RefreshTokenDTO("previous-refresh-token");
        when(authService.refreshTokens(request)).thenThrow(new AccountNotActivatedException());

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie(REFRESH_TOKEN_COOKIE, "previous-refresh-token")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_NOT_ACTIVATED"))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));

        verify(authService).refreshTokens(request);
    }

    private void assertRefreshCookie(MvcResult result, String expectedValue, String expectedMaxAge) {
        String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);

        assertAll(
                () -> assertNotNull(setCookie),
                () -> assertTrue(setCookie.startsWith(REFRESH_TOKEN_COOKIE + "=" + expectedValue + ";")),
                () -> assertTrue(setCookie.contains("Path=/api/auth;")),
                () -> assertTrue(setCookie.contains(expectedMaxAge)),
                () -> assertTrue(setCookie.contains("Secure")),
                () -> assertTrue(setCookie.contains("HttpOnly")),
                () -> assertTrue(setCookie.contains("SameSite=Strict"))
        );
    }
}
