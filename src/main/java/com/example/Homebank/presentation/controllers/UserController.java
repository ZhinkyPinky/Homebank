package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.UserService;
import com.example.Homebank.presentation.ApiPaths;
import com.example.Homebank.presentation.dto.ChangePasswordDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.USERS)
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @PostMapping(ApiPaths.CHANGE_PASSWORD)
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        logger.info("Request to change password received.");

        userService.changePassword(changePasswordDTO);
        return ResponseEntity.ok().build();
    }
}
