package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.AccountRecoveryService;
import com.example.Homebank.presentation.ApiPaths;
import com.example.Homebank.presentation.dto.auth.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsible for account recovery actions:
 * recovery initiation, recovery authentication, and password reset.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.ACCOUNT_RECOVERY)
public class AccountRecoveryController {
    private static final Logger logger = LoggerFactory.getLogger(AccountRecoveryController.class);

    private final AccountRecoveryService accountRecoveryService;

    /**
     * Handles requests to initiate the recovery of an account.
     *
     * @param emailDTO E-mail of the user that wants to recover their account.
     * @return Always {@code 200 OK} for valid input to avoid leaking account existence.
     */
    @PostMapping(ApiPaths.INITIATE_RECOVERY)
    public ResponseEntity<String> initiateUserAccountRecovery(@Valid @RequestBody EmailDTO emailDTO) {
        logger.info("Request to initiate recovery for user account with e-mail: {} received.", emailDTO.email());

        accountRecoveryService.initiateAccountRecovery(emailDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * Handles requests to authenticate a user wanting to recover their account.
     *
     * @param authenticationDTO E-mail and recovery password.
     * @return A recovery token if successful.
     */
    @PostMapping(ApiPaths.AUTHENTICATE)
    public ResponseEntity<RecoveryTokenDTO> authenticate(@Valid @RequestBody AuthenticationDTO authenticationDTO) {
        logger.info("Request to authenticate user with e-mail: {} received.", authenticationDTO.email());

        RecoveryTokenDTO recoveryTokenDTO = accountRecoveryService.authenticate(authenticationDTO);
        return ResponseEntity.ok(recoveryTokenDTO);
    }

    /**
     * Handles requests to set a new password using a valid recovery token.
     *
     * @param setNewPasswordDTO Recovery token and new password data.
     * @return the new access token and a refresh token in a secure, HTTP-only cookie.
     */
    @PostMapping(ApiPaths.SET_NEW_PASSWORD)
    public ResponseEntity<AccessTokenDTO> setNewPassword(@Valid @RequestBody SetNewPasswordDTO setNewPasswordDTO) {
        logger.info("Request to set new password received.");

        AccessAndRefreshTokenDTO tokenDTO = accountRecoveryService.setNewPassword(setNewPasswordDTO);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokenDTO.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path(ApiPaths.AUTH_BASE)
                .maxAge(tokenDTO.refreshTokenDuration())
                .sameSite("Strict")
                .build();

        AccessTokenDTO accessTokenDTO = new AccessTokenDTO(
                tokenDTO.accessToken(),
                tokenDTO.message(),
                tokenDTO.accountStatus()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(accessTokenDTO);

    }
}
