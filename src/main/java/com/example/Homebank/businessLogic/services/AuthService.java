package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.email.EmailService;
import com.example.Homebank.businessLogic.security.AccessJwtUtil;
import com.example.Homebank.businessLogic.security.RefreshJwtUtil;
import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.repositories.UserRepository;
import com.example.Homebank.presentation.dto.*;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserService userService;
    private final EmailService emailService;

    /**
     * Authenticate a user based on username and password. Generates access and refresh tokens if authentication is
     * successful. The refresh token is saved encrypted in the DB.
     *
     * @param authenticationDTO Username and password.
     * @return Access and refresh tokens.
     */
    @Transactional
    public AccessAndRefreshTokenDTO authenticate(AuthenticationDTO authenticationDTO) {
        logger.info("Attempting to authenticate user: {}", authenticationDTO.username());

        String username = authenticationDTO.username();
        String password = authenticationDTO.password();

        UsernamePasswordAuthenticationToken authenticationToken = UsernamePasswordAuthenticationToken.unauthenticated(username, password);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        String accessToken = accessJwtUtil.generateToken(userEntity.getUsername());
        String refreshToken = refreshJwtUtil.generateToken(userEntity.getUsername());
        String encryptedRefreshToken = passwordEncoder.encode(refreshToken);

        userEntity.setUserToken(encryptedRefreshToken);
        userRepository.save(userEntity);

        logger.info("User {} authenticated successfully. Tokens generated.", userEntity.getUsername());

        return new AccessAndRefreshTokenDTO(accessToken, refreshToken, "Login successful");
    }

    /**
     * Registers a new user.
     *
     * @param registrationDTO Username, e-mail and password.
     * @return Access and refresh tokens.
     */
    @Transactional
    public AccessAndRefreshTokenDTO register(RegistrationDTO registrationDTO) {
        logger.info("Attempting to register new user: {}", registrationDTO.username());

        validateRegistrationDetails(registrationDTO);

        String username = registrationDTO.username();
        String password = registrationDTO.password();
        String encodedPassword = passwordEncoder.encode(password);
        String email = registrationDTO.email();

        String accessToken = accessJwtUtil.generateToken(username);
        String refreshToken = refreshJwtUtil.generateToken(username);
        LocalDateTime refreshTokenExpirationDate = LocalDateTime.ofInstant(refreshJwtUtil.extractExpirationDate(refreshToken).toInstant(), java.time.ZoneId.systemDefault());
        String encryptedRefreshToken = passwordEncoder.encode(refreshToken);

        //TODO: Needs a procedure?
        LocalDateTime currentDateTime = LocalDateTime.now();

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword(encodedPassword);
        userEntity.setEmail(email);
        userEntity.setUserToken(encryptedRefreshToken);
        userEntity.setNextUserTokenChangeDate(refreshTokenExpirationDate); //TODO: Remove column.
        userEntity.setTypeOfUserCode("ENDUSER");
        userEntity.setRowCreatedDate(currentDateTime);
        userEntity.setRowLastEditDate(currentDateTime);
        userEntity.setRowVersion(currentDateTime);

        userRepository.save(userEntity);

        logger.info("User {} registered successfully. Tokens generated.", username);

        //TODO: Send confirmation e-mail to activate account instead of returning tokens.
        return new AccessAndRefreshTokenDTO(accessToken, refreshToken, "Registration successful");
    }

    /**
     * Validates the details provided for registration by checking:
     * 1. If the username is taken.
     * 2. If the e-mail is taken.
     *
     * @param registrationDTO Username, e-mail and password.
     */
    private void validateRegistrationDetails(RegistrationDTO registrationDTO) {
        logger.debug("Validation registration details for user: {}", registrationDTO.username());

        if (userRepository.findByUsername(registrationDTO.username()).isPresent()) {
            logger.error("Username {} already exists", registrationDTO.username());
            throw new EntityExistsException("Username already exists");
        }

        if (userRepository.findByEmail(registrationDTO.email()).isPresent()) {
            logger.error("Email {} already exists", registrationDTO.email());
            throw new EntityExistsException("Email already exists");
        }

        logger.debug("Registration details validated successfully for user: {}", registrationDTO.username());
    }

    /**
     * Generates new access and refresh tokens for a user provided that the refresh token is valid.
     *
     * @param refreshTokenDTO Refresh token.
     * @return New access and refresh token.
     */
    @Transactional
    public AccessAndRefreshTokenDTO refreshTokens(RefreshTokenDTO refreshTokenDTO) {
        String refreshToken = refreshTokenDTO.refreshToken();
        String username = refreshJwtUtil.extractUsername(refreshToken);

        logger.info("Attempting to refresh tokens for user: {}", username);

        UserEntity userEntity = (UserEntity) userService.loadUserByUsername(username);

        if (!refreshJwtUtil.isTokenValid(refreshToken, userEntity)) {
            logger.error("Invalid refresh token: {}", refreshToken);
            throw new IllegalArgumentException("Invalid refresh token");
        }

        if (!passwordEncoder.matches(refreshToken, userEntity.getUserToken())) {
            logger.error("Failed to authenticate user with username: {} since the provided refresh token does not match the stored token", username);
            throw new BadCredentialsException("Bad credentials");
        }

        String newAccessToken = accessJwtUtil.generateToken(username);
        String newRefreshToken = refreshJwtUtil.generateToken(username);
        LocalDateTime newRefreshTokenExpirationDate = LocalDateTime.ofInstant(refreshJwtUtil.extractExpirationDate(newRefreshToken).toInstant(), java.time.ZoneId.systemDefault());
        String encryptedNewRefreshToken = passwordEncoder.encode(newRefreshToken);

        userEntity.setUserToken(encryptedNewRefreshToken);
        userEntity.setNextUserTokenChangeDate(newRefreshTokenExpirationDate);
        userRepository.save(userEntity);

        logger.info("Tokens refreshed successfully for user: {}", username);

        return new AccessAndRefreshTokenDTO(newAccessToken, newRefreshToken, "Tokens refreshed");
    }

    /**
     * Sign out a user by removing the saved refresh token from the DB.
     *
     * @param refreshToken Provided refresh token.
     */
    @Transactional
    public void signOut(String refreshToken) {
        String username = refreshJwtUtil.extractUsername(refreshToken);

        logger.info("Attempting to sign out user: {}", username);

        UserEntity userEntity = (UserEntity) userService.loadUserByUsername(username);
        userEntity.setUserToken(null);
        userRepository.save(userEntity);

        logger.info("User {} signed out successfully.", username);
    }
}
