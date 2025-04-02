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
     * @param authenticationRequest Request body containing username and password.
     * @return a response containing access and refresh tokens if successful, otherwise an error response.
     */
    @PostMapping(ApiPaths.SIGN_IN)
    public ResponseEntity<AuthenticationResponseDTO> signIn(@Valid @RequestBody AuthenticationRequestDTO authenticationRequest) {
        logger.info("Sign in request received for user: {}", authenticationRequest.username());
        AuthenticationResponseDTO responseBody = authService.authenticate(authenticationRequest);

        return ResponseEntity.ok(responseBody);
    }

    /**
     * Handles requests to sign out a user.
     *
     * @param signOutRequest Request body containing a refresh token.
     * @return a response indicating whether the request was successful.
     */
    @PostMapping(ApiPaths.SIGN_OUT)
    public ResponseEntity<String> logout(@Valid @RequestBody SignOutRequest signOutRequest) {
        logger.info("Sign out request received.");
        //TODO: Implement.
        authService.signOut(signOutRequest.refreshToken());
        return ResponseEntity.ok().build();
    }

    /**
     * Handles requests to register a new user.
     *
     * @param registrationRequest Request body containing username, password and e-mail.
     * @return a response indicating whether the request was successful.
     */
    @PostMapping(ApiPaths.REGISTER)
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest registrationRequest) {
        //TODO: Don't return tokens on registration, send confirmation e-mail instead to unlock the user account.
        logger.info("Registration request received for user: {}", registrationRequest.username());
        return ResponseEntity.ok(authService.register(registrationRequest));
    }

    /**
     * Handles requests to refresh JWTs.
     *
     * @param refreshRequest Request body containing a refresh token.
     * @return the new access and refresh tokens.
     */
    @PostMapping(ApiPaths.REFRESH)
    public ResponseEntity<AuthenticationResponseDTO> refresh(@Valid @RequestBody RefreshRequest refreshRequest) {
        logger.info("Refresh request received.");
        return ResponseEntity.ok(authService.refreshToken(refreshRequest));
    }

}
