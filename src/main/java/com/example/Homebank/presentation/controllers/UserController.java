package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.AccountRecoveryService;
import com.example.Homebank.businessLogic.services.UserService;
import com.example.Homebank.presentation.ApiPaths;
import com.example.Homebank.presentation.dto.ChangePasswordDTO;
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
}
