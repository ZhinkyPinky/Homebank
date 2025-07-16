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

    /**
     * Handles requests to sign in a user.
     *
     * @param authenticationRequest Request body containing email and password.
     * @return a response containing access and refresh tokens if successful, otherwise an error response.
     */
    @PostMapping(ApiPaths.SIGN_IN)
    public ResponseEntity<AccessAndRefreshTokenDTO> signIn(@Valid @RequestBody AuthenticationDTO authenticationRequest) {
        logger.info("Sign in request received for user: {}", authenticationRequest.email());
        AccessAndRefreshTokenDTO responseBody = authService.authenticate(authenticationRequest);

        return ResponseEntity.ok(responseBody);
    }

    /**
     * Handles requests to sign out a user.
     *
     * @param refreshTokenDTO Request body containing a refresh token.
     * @return a response indicating whether the request was successful.
     */
    @PostMapping(ApiPaths.SIGN_OUT)
    public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) {
        logger.info("Sign out request received.");
        //TODO: Implement.
        authService.signOut(refreshTokenDTO.refreshToken());
        return ResponseEntity.ok().build();
    }

    /**
     * Handles requests to register a new user.
     *
     * @param registrationDTO Request body containing username, password and e-mail.
     * @return a response indicating whether the request was successful.
     */
    @PostMapping(ApiPaths.REGISTER)
    public ResponseEntity<AccessAndRefreshTokenDTO> register(@Valid @RequestBody RegistrationDTO registrationDTO) {
        //TODO: Don't return tokens on registration, send confirmation e-mail instead to unlock the user account.
        logger.info("Registration request received for user: {}", registrationDTO.email());
        return ResponseEntity.ok(authService.register(registrationDTO));
    }

    /**
     * Handles requests to refresh JWTs.
     *
     * @param refreshTokenDTO Request body containing a refresh token.
     * @return the new access and refresh tokens.
     */
    @PostMapping(ApiPaths.REFRESH)
    public ResponseEntity<AccessAndRefreshTokenDTO> refresh(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) {
        logger.info("Refresh request received.");
        return ResponseEntity.ok(authService.refreshTokens(refreshTokenDTO));
    }

}
