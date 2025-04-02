package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.UserService;
import com.example.Homebank.presentation.ApiPaths;
import com.example.Homebank.presentation.dto.ChangePasswordDTO;
import com.example.Homebank.presentation.dto.EmailDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.USERS)
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    /**
     * Handles requests to change the password of a user account.
     *
     * @param changePasswordDTO The old password and the new password twice to confirm that it is correct.
     * @return Response indicating whether the request was successful or not.
     */
    @PostMapping(ApiPaths.CHANGE_PASSWORD)
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        logger.info("Request to change password received.");

        userService.changePassword(changePasswordDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * Handles requests to initiate the recovery of an account.
     *
     * @param emailDTO E-mail of the user that wants to recover their account.
     * @return Response indicating whether the request was successful or not.
     */
    @PostMapping(ApiPaths.INITIATE_RECOVERY)
    public ResponseEntity<String> initiateUserAccountRecovery(@RequestBody EmailDTO emailDTO) {
        logger.info("Request to initiate recovery for user account with e-mail: {} received.", emailDTO.email());
        userService.initiateUserAccountRecovery(emailDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * Handles requests to recover an account.
     *
     * @param recoveryToken Token used to confirm that the user wants to recover their account.
     * @return Response indicating whether the request was successful or not.
     */
    @GetMapping(ApiPaths.RECOVER)
    public ResponseEntity<String> recoverUserAccount(@RequestParam String recoveryToken) {
        logger.info("Request to recover user account with recovery token: {} received.", recoveryToken);
        userService.recoverUserAccount(recoveryToken);
        return ResponseEntity.ok("An e-mail with a recovery password should be sent soon.");
    }
}
