package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.AuthService;
import com.example.Homebank.presentation.ApiPaths;
import com.example.Homebank.presentation.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.AUTH)
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @PostMapping(ApiPaths.LOGIN)
    public ResponseEntity<AuthenticationResponseDTO> login(@Valid @RequestBody AuthenticationRequestDTO authenticationRequestDTO) {
        logger.info("Login request received for user: {}", authenticationRequestDTO.username());
        return ResponseEntity.ok(authService.authenticate(authenticationRequestDTO));
    }

    @PostMapping(ApiPaths.LOGIN)
    public ResponseEntity<String> logout() {
        //TODO: Implement.
        logger.info("Logout request received.");
        return ResponseEntity.ok().build();
    }

    @PostMapping(ApiPaths.REGISTER)
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest registrationRequest) {
        logger.info("Registration request received for user: {}", registrationRequest.username());
        return ResponseEntity.ok(authService.register(registrationRequest));
    }

    @PostMapping(ApiPaths.REFRESH)
    public ResponseEntity<AuthenticationResponseDTO> refresh(@Valid @RequestBody RefreshRequest refreshRequest) {
        logger.info("Refresh request received.");
        return ResponseEntity.ok(authService.refreshToken(refreshRequest));
    }

}
