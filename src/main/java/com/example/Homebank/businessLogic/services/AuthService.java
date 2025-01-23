package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.security.AccessJwtUtil;
import com.example.Homebank.businessLogic.security.RefreshJwtUtil;
import com.example.Homebank.dataAccess.entities.User;
import com.example.Homebank.dataAccess.repositories.UserRepository;
import com.example.Homebank.presentation.bodies.*;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final RefreshJwtUtil refreshJwtUtil;
    private final AccessJwtUtil accessJwtUtil;
    private final UserRepository userRepository;

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        logger.info("Attempting to authenticate user: {}", authenticationRequest.username());

        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(authenticationRequest.username(), authenticationRequest.password())
        );

        User user = ((User) authentication.getPrincipal());
        String accessToken = accessJwtUtil.generateToken(user.getUsername());
        String refreshToken = refreshJwtUtil.generateToken(user.getUsername());

        user.setUserToken(refreshToken);
        userRepository.save(user);

        logger.info("User {} authenticated successfully. Tokens generated.", user.getUsername());

        return new AuthenticationResponse(accessToken, refreshToken, "Login successful");
    }

    public RegistrationResponse register(RegistrationRequest registrationRequest) {
        logger.info("Attempting to register new user: {}", registrationRequest.username());

        validateRegistrationDetails(registrationRequest);

        String username = registrationRequest.username();
        String accessToken = accessJwtUtil.generateToken(username);
        String refreshToken = refreshJwtUtil.generateToken(username);

        //TODO: Needs a procedure?
        User user = new User();
        user.setUsername(registrationRequest.username());
        user.setPassword(passwordEncoder.encode(registrationRequest.password()));
        user.setEmail(registrationRequest.email());
        user.setUserToken(refreshToken);
        user.setNextUserTokenChangeDate(LocalDateTime.now()); //TODO: Remove column.
        user.setTypeOfUserCode("ENDUSER");
        user.setRowCreatedDate(LocalDateTime.now());
        user.setRowLastEditDate(LocalDateTime.now());
        user.setRowVersion(LocalDateTime.now());

        userRepository.save(user);

        logger.info("User {} registered successfully. Tokens generated.", username);

        return new RegistrationResponse(accessToken, refreshToken, "Registration successful");
    }

    private void validateRegistrationDetails(RegistrationRequest registrationRequest) {
        logger.debug("Validation registration details for user: {}", registrationRequest.username());

        if (userRepository.findByUsername(registrationRequest.username()).isPresent()) {
            logger.error("Username {} already exists", registrationRequest.username());
            throw new EntityExistsException("Username already exists");
        }

        if (userRepository.findByPassword(registrationRequest.password()).isPresent()) {
            logger.error("Password already exists");
            throw new EntityExistsException("Password already exists");
        }

        if (userRepository.findByEmail(registrationRequest.email()).isPresent()) {
            logger.error("Email {} already exists", registrationRequest.email());
            throw new EntityExistsException("Email already exists");
        }

        logger.debug("Registration details validated successfully for user: {}", registrationRequest.username());
    }

    public AuthenticationResponse refreshToken(RefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.refreshToken();
        String username = refreshJwtUtil.extractUsername(refreshToken);

        logger.info("Attempting to refresh tokens for user: {}", username);

        User user = userRepository.findByUserToken(refreshRequest.refreshToken()).orElseThrow(() -> {
            logger.error("User not found for refresh token: {}", refreshRequest.refreshToken());
            return new EntityNotFoundException("User not found");
        });


        if (!refreshJwtUtil.isTokenValid(refreshToken, user)) {
            logger.error("Invalid refresh token: {}", refreshToken);
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String newAccessToken = accessJwtUtil.generateToken(username);
        String newRefreshToken = refreshJwtUtil.generateToken(username);

        user.setUserToken(newRefreshToken);
        userRepository.save(user);

        logger.info("Tokens refreshed successfully for user: {}", username);

        return new AuthenticationResponse(newAccessToken, newRefreshToken, "Tokens refreshed");
    }
}
