package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.AuthService;
import com.example.Homebank.dataAccess.entities.UserStatus;
import com.example.Homebank.presentation.ApiPaths;
import com.example.Homebank.presentation.dto.auth.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

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
     * @return a response containing the access token and account status. Sets a secure, HTTP-only
     *         refresh-token cookie for active accounts and deletes it for accounts pending activation.
     */
    @PostMapping(ApiPaths.SIGN_IN)
    public ResponseEntity<AccessTokenDTO> signIn(@Valid @RequestBody AuthenticationDTO authenticationRequest) {
        logger.info("Sign in request received for user: {}", authenticationRequest.email());
        AccessAndRefreshTokenDTO tokenInformation = authService.authenticate(authenticationRequest);

        ResponseCookie refreshTokenCookie = UserStatus.ACTIVE.name().equals(tokenInformation.accountStatus()) ? createRefreshTokenCookie(tokenInformation) : deleteRefreshTokenCookie();
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO(
                tokenInformation.accessToken(),
                tokenInformation.message(),
                tokenInformation.accountStatus()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(accessTokenDTO);
    }

    /**
     * Handles requests to sign out a user.
     *
     * @return an empty response that deletes the refresh-token cookie.
     */
    @PostMapping(ApiPaths.SIGN_OUT)
    public ResponseEntity<String> logout(HttpServletResponse response, HttpServletRequest request) {
        logger.info("Sign out request received.");

        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshTokenCookie().toString());

        Cookie cookie = WebUtils.getCookie(request, "refreshToken");
        String refreshToken = cookie != null ? cookie.getValue() : null;
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.signOut(refreshToken);
        }

        return ResponseEntity.noContent().build();
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
     * Handles requests to refresh authentication tokens.
     *
     * @return the new access token and a rotated refresh token in a secure, HTTP-only cookie.
     */
    @PostMapping(ApiPaths.REFRESH)
    public ResponseEntity<AccessTokenDTO> refresh(HttpServletRequest request) {
        logger.info("Refresh request received.");
        Cookie incomingRefreshTokenCookie = WebUtils.getCookie(request, "refreshToken");
        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO(incomingRefreshTokenCookie != null ? incomingRefreshTokenCookie.getValue() : null);

        AccessAndRefreshTokenDTO accessAndRefreshTokenDTO = authService.refreshTokens(refreshTokenDTO);

        ResponseCookie outgoingRefreshTokenCookie = UserStatus.ACTIVE.name().equals(accessAndRefreshTokenDTO.accountStatus()) ? createRefreshTokenCookie(accessAndRefreshTokenDTO) : deleteRefreshTokenCookie();
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO(
                accessAndRefreshTokenDTO.accessToken(),
                accessAndRefreshTokenDTO.message(),
                accessAndRefreshTokenDTO.accountStatus()
        );

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, outgoingRefreshTokenCookie.toString()).body(accessTokenDTO);
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
     * @return A response indicating whether the request was successful.
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


    private ResponseCookie createRefreshTokenCookie(AccessAndRefreshTokenDTO tokenInformation) {
        return ResponseCookie.from("refreshToken", tokenInformation.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path(ApiPaths.AUTH_BASE)
                .maxAge(tokenInformation.refreshTokenDuration())
                .sameSite("Strict")
                .build();
    }

    /**
     * Creates a ResponseCookie that deletes the refresh token cookie.
     *
     * @return a ResponseCookie that deletes the refresh token cookie.
     */
    private ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path(ApiPaths.AUTH_BASE)
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }
}
