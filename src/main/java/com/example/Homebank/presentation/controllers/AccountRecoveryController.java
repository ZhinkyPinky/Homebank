package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.AccountRecoveryService;
import com.example.Homebank.presentation.ApiPaths;
import com.example.Homebank.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * @return Response indicating whether the request was successful or not.
     */
    @PostMapping(ApiPaths.INITIATE_RECOVERY)
    public ResponseEntity<String> initiateUserAccountRecovery(@RequestBody EmailDTO emailDTO) {
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
    public ResponseEntity<RecoveryTokenDTO> authenticate(@RequestBody AuthenticationDTO authenticationDTO) {
        logger.info("Request to authenticate user with e-mail: {} received.", authenticationDTO.email());

        RecoveryTokenDTO recoveryTokenDTO = accountRecoveryService.authenticate(authenticationDTO);
        return ResponseEntity.ok(recoveryTokenDTO);
    }

    @PostMapping(ApiPaths.SET_NEW_PASSWORD)
    public ResponseEntity<AccessAndRefreshTokenDTO> setNewPassword(@RequestBody SetNewPasswordDTO setNewPasswordDTO) {
        logger.info("Request to set new password received.");

        AccessAndRefreshTokenDTO accessAndRefreshTokenDTO = accountRecoveryService.setNewPassword(setNewPasswordDTO);
        return ResponseEntity.ok(accessAndRefreshTokenDTO);
    }
}
