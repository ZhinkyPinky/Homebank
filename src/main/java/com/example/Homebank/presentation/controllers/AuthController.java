package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.AuthService;
import com.example.Homebank.presentation.ApiPaths;
import com.example.Homebank.presentation.bodies.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.AUTH_BASE_PATH)
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @PostMapping(ApiPaths.LOGIN_PATH)
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest authenticationRequest) {
        logger.info("Login request received for user: {}", authenticationRequest.username());
        return ResponseEntity.ok(authService.authenticate(authenticationRequest));
    }

    @PostMapping(ApiPaths.REGISTER_PATH)
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest registrationRequest) {
        logger.info("Registration request received for user: {}", registrationRequest.username());
        return ResponseEntity.ok(authService.register(registrationRequest));
    }

    @PostMapping(ApiPaths.REFRESH_PATH)
    public ResponseEntity<AuthenticationResponse> refresh(@Valid @RequestBody RefreshRequest refreshRequest) {
        logger.info("Refresh request received.");
        return ResponseEntity.ok(authService.refreshToken(refreshRequest));
    }

}
