package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.security.AccessJwtUtil;
import com.example.Homebank.businessLogic.security.RefreshJwtUtil;
import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.repositories.UserRepository;
import com.example.Homebank.presentation.dto.*;
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

    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO authenticationRequestDTO) {
        logger.info("Attempting to authenticate user: {}", authenticationRequestDTO.username());

        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(authenticationRequestDTO.username(), authenticationRequestDTO.password())
        );

        UserEntity userEntity = ((UserEntity) authentication.getPrincipal());
        String accessToken = accessJwtUtil.generateToken(userEntity.getUsername());
        String refreshToken = refreshJwtUtil.generateToken(userEntity.getUsername());

        userEntity.setUserToken(refreshToken);
        userRepository.save(userEntity);

        logger.info("User {} authenticated successfully. Tokens generated.", userEntity.getUsername());

        return new AuthenticationResponseDTO(accessToken, refreshToken, "Login successful");
    }

    public RegistrationResponse register(RegistrationRequest registrationRequest) {
        logger.info("Attempting to register new user: {}", registrationRequest.username());

        validateRegistrationDetails(registrationRequest);

        String username = registrationRequest.username();
        String accessToken = accessJwtUtil.generateToken(username);
        String refreshToken = refreshJwtUtil.generateToken(username);

        //TODO: Needs a procedure?
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(registrationRequest.username());
        userEntity.setPassword(passwordEncoder.encode(registrationRequest.password()));
        userEntity.setEmail(registrationRequest.email());
        userEntity.setUserToken(refreshToken);
        userEntity.setNextUserTokenChangeDate(LocalDateTime.now()); //TODO: Remove column.
        userEntity.setTypeOfUserCode("ENDUSER");
        userEntity.setRowCreatedDate(LocalDateTime.now());
        userEntity.setRowLastEditDate(LocalDateTime.now());
        userEntity.setRowVersion(LocalDateTime.now());

        userRepository.save(userEntity);

        logger.info("User {} registered successfully. Tokens generated.", username);

        return new RegistrationResponse(accessToken, refreshToken, "Registration successful");
    }

    private void validateRegistrationDetails(RegistrationRequest registrationRequest) {
        logger.debug("Validation registration details for user: {}", registrationRequest.username());

        if (userRepository.findByUsername(registrationRequest.username()).isPresent()) {
            logger.error("Username {} already exists", registrationRequest.username());
            throw new EntityExistsException("Username already exists");
        }

        if (userRepository.findByPassword(passwordEncoder.encode(registrationRequest.password())).isPresent()) {
            logger.error("Password already exists");
            throw new EntityExistsException("Password already exists");
        }

        if (userRepository.findByEmail(registrationRequest.email()).isPresent()) {
            logger.error("Email {} already exists", registrationRequest.email());
            throw new EntityExistsException("Email already exists");
        }

        logger.debug("Registration details validated successfully for user: {}", registrationRequest.username());
    }

    public AuthenticationResponseDTO refreshToken(RefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.refreshToken();
        String username = refreshJwtUtil.extractUsername(refreshToken);

        logger.info("Attempting to refresh tokens for user: {}", username);

        UserEntity userEntity = userRepository.findByUserToken(refreshRequest.refreshToken()).orElseThrow(() -> {
            logger.error("User not found for refresh token: {}", refreshRequest.refreshToken());
            return new EntityNotFoundException("User not found");
        });


        if (!refreshJwtUtil.isTokenValid(refreshToken, userEntity)) {
            logger.error("Invalid refresh token: {}", refreshToken);
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String newAccessToken = accessJwtUtil.generateToken(username);
        String newRefreshToken = refreshJwtUtil.generateToken(username);

        userEntity.setUserToken(newRefreshToken);
        userRepository.save(userEntity);

        logger.info("Tokens refreshed successfully for user: {}", username);

        return new AuthenticationResponseDTO(newAccessToken, newRefreshToken, "Tokens refreshed");
    }

    public boolean logout() {

        return true;
    }
}
