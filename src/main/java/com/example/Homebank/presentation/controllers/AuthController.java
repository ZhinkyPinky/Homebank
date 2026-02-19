package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.AuthService;
import com.example.Homebank.presentation.ApiPaths;
import com.example.Homebank.presentation.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsible for handling authentication-related requests, such as signing in, signing out, registering and refreshing tokens.
 */
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
    public ResponseEntity<String> register(@Valid @RequestBody RegistrationDTO registrationDTO) {
        logger.info("Registration request received for user: {}", registrationDTO.email());
        authService.register(registrationDTO);
        return ResponseEntity.ok("Registration successful. Please check your e-mail to activate your account.");
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

    /**
     * Handles requests to activate a user account.
     *
     * @param token Activation token sent to the user's e-mail.
     * @return a response indicating whether the request was successful.
     */
    @GetMapping(ApiPaths.ACTIVATE)
    public ResponseEntity<String> activateAccount(@RequestParam String token) {
        logger.info("Account activation request received.");
        authService.activateAccount(token);
        return ResponseEntity.ok("Account activated successfully");
    }

    /**
     * Handles requests to resend the activation e-mail.
     *
     * @param authentication Authenticated user details.
     * @return a response indicating whether the request was successful.
     */
    @PostMapping(ApiPaths.RESEND_ACTIVATION)
    public ResponseEntity<String> resendActivationEmail(Authentication authentication) {
        logger.info("Resend activation email request received.");
        try {
            authService.resendActivationEmail(authentication.getName());
        } catch (RuntimeException e) {
            logger.error("Failed to process resend activation email request.", e);
        }
        return ResponseEntity.ok("Activation email resent successfully. Please check your e-mail.");
    }

}
