package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.security.OpaqueTokenGenerator;
import com.example.Homebank.businessLogic.security.TokenHasher;
import com.example.Homebank.businessLogic.services.email.EmailService;
import com.example.Homebank.businessLogic.security.AccessJwtUtil;
import com.example.Homebank.businessLogic.security.RefreshJwtUtil;
import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.repositories.UserRepository;
import com.example.Homebank.presentation.ApiPaths;
import com.example.Homebank.presentation.dto.*;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for handling authentication and registration of users. Also handles refreshing of tokens and signing out.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    private final RefreshJwtUtil refreshJwtUtil; //TODO: Use opaque token for refresh tokens instead of JWT.
    private final AccessJwtUtil accessJwtUtil;

    private final UserRepository userRepository;
    private final UserService userService;
    private final EmailService emailService;

    // Duration in days.
    @Value("${jwt.refresh.duration}")
    private String REFRESH_DURATION;

    @Value("${application.url}")
    private String applicationURL;

    @Value("${auth.activation-token.duration-hours:24}")
    private long activationTokenDurationHours;

    /**
     * Authenticate a user based on username and password. Generates access and refresh tokens if authentication is
     * successful. The refresh token is saved encrypted in the DB.
     *
     * @param authenticationDTO Username and password.
     * @return Access and refresh tokens.
     */
    @Transactional
    public AccessAndRefreshTokenDTO authenticate(AuthenticationDTO authenticationDTO) {
        logger.info("Attempting to authenticate user: {}", authenticationDTO.email());

        String email = authenticationDTO.email();
        String password = authenticationDTO.password();

        UsernamePasswordAuthenticationToken authenticationToken = UsernamePasswordAuthenticationToken.unauthenticated(email, password);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        String accessToken = accessJwtUtil.generateToken(userEntity.getUsername());
        //TODO: Use opaque token for refresh tokens instead of JWT.
        String refreshToken = refreshJwtUtil.generateToken(userEntity.getUsername());
        LocalDateTime refreshTokenExpirationDate = LocalDateTime.now().plusDays(Long.parseLong(REFRESH_DURATION));

        userEntity.setRefreshToken(refreshToken);
        userEntity.setNextRefreshTokenExpirationDate(refreshTokenExpirationDate);
        userRepository.save(userEntity);

        logger.info("User {} authenticated successfully. Tokens generated.", userEntity.getUsername());

        return new AccessAndRefreshTokenDTO(accessToken, refreshToken, "Login successful");
    }

    /**
     * Registers a new user.
     *
     * @param registrationDTO Email and password.
     */
    @Transactional
    public void register(RegistrationDTO registrationDTO) {
        logger.info("Attempting to register new user: {}", registrationDTO.email());

        validateRegistrationDetails(registrationDTO);

        String email = registrationDTO.email();
        String password = registrationDTO.password();
        String encodedPassword = passwordEncoder.encode(password);

        //TODO: Use opaque token for refresh tokens instead of JWT.
        String refreshToken = refreshJwtUtil.generateToken(email);
        String hashedRefreshToken = TokenHasher.hash(refreshToken);
        LocalDateTime refreshTokenExpirationDate = LocalDateTime.now().plusDays(Long.parseLong(REFRESH_DURATION));

        String activationToken = OpaqueTokenGenerator.generateToken();
        String hashedActivationToken = TokenHasher.hash(activationToken);
        LocalDateTime activationTokenExpirationDate = LocalDateTime.now().plusHours(activationTokenDurationHours);


        LocalDateTime currentDateTime = LocalDateTime.now();

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(email);
        userEntity.setPassword(encodedPassword);
        userEntity.setRefreshToken(hashedRefreshToken);
        userEntity.setNextRefreshTokenExpirationDate(refreshTokenExpirationDate); //TODO: Remove column.
        userEntity.setTypeOfUserCode("ENDUSER");
        userEntity.setActivationToken(hashedActivationToken);
        userEntity.setActivationTokenExpiration(activationTokenExpirationDate);
        userEntity.setRowCreatedDate(currentDateTime);
        userEntity.setRowLastEditDate(currentDateTime);
        userEntity.setRowVersion(currentDateTime);

        userRepository.save(userEntity);

        logger.info("User {} registered successfully. Tokens generated.", email);


        String activationLink = applicationURL + ApiPaths.AUTH + ApiPaths.ACTIVATE + "?token=" + activationToken;
        emailService.sendEmail(email, "Activate your account", "Please click the following link to activate your account: " + activationLink);
    }

    /**
     * Validates the details provided for registration by checking:
     * 1. If the email is taken.
     * 2. If the e-mail is taken.
     *
     * @param registrationDTO Email and password.
     */
    private void validateRegistrationDetails(RegistrationDTO registrationDTO) {
        logger.debug("Validation registration details for user: {}", registrationDTO.email());

        if (userRepository.findByEmail(registrationDTO.email()).isPresent()) {
            logger.error("Email {} already exists", registrationDTO.email());
            throw new EntityExistsException("Email already exists");
        }

        logger.debug("Registration details validated successfully for user: {}", registrationDTO.email());
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
        String email = refreshJwtUtil.extractEmail(refreshToken);

        logger.info("Attempting to refresh tokens for user: {}", email);

        UserEntity userEntity = (UserEntity) userService.loadUserByUsername(email);

        if (!refreshJwtUtil.isTokenValid(refreshToken, userEntity.getUsername())) {
            logger.error("Invalid refresh token: {}", refreshToken);
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // TODO: Fix so that the refresh token stored in the DB is hashed and the provided refresh token is hashed before comparing.

        String newAccessToken = accessJwtUtil.generateToken(email);
        //TODO: Use opaque token for refresh tokens instead of JWT.
        String newRefreshToken = refreshJwtUtil.generateToken(email);
        String hashedNewRefreshToken = TokenHasher.hash(newRefreshToken);
        LocalDateTime newRefreshTokenExpirationDate = LocalDateTime.now().plusDays(Long.parseLong(REFRESH_DURATION));

        userEntity.setRefreshToken(hashedNewRefreshToken);
        userEntity.setNextRefreshTokenExpirationDate(newRefreshTokenExpirationDate);
        userRepository.save(userEntity);

        logger.info("Tokens refreshed successfully for user: {}", email);

        return new AccessAndRefreshTokenDTO(newAccessToken, newRefreshToken, "Tokens refreshed");
    }

    /**
     * Sign out a user by removing the saved refresh token from the DB.
     *
     * @param refreshToken Provided refresh token.
     */
    @Transactional
    public void signOut(String refreshToken) {
        String email = refreshJwtUtil.extractEmail(refreshToken);

        logger.info("Attempting to sign out user: {}", email);

        UserEntity userEntity = (UserEntity) userService.loadUserByUsername(email);
        userEntity.setRefreshToken(null);
        userRepository.save(userEntity);

        logger.info("User {} signed out successfully.", email);
    }

    /**
     * Activates a user account by validating the provided activation token, removing it from the DB if valid.
     *
     * @param token Activation token sent to the user's e-mail.
     */
    @Transactional
    public void activateAccount(String token) {
        logger.info("Attempting to activate account.");

        String hashedToken = TokenHasher.hash(token);

        UserEntity userEntity = userRepository.findByActivationToken(hashedToken).orElseThrow(() -> {
            logger.error("Invalid activation token.");
            return new IllegalArgumentException("Invalid activation token");
        });

        LocalDateTime activationTokenExpiryDate = userEntity.getActivationTokenExpiration();
        if (activationTokenExpiryDate == null) {
            logger.error("Activation token expiration is missing for user: {}", userEntity.getEmail());
            throw new IllegalArgumentException("Invalid activation token");
        }
        if (LocalDateTime.now().isAfter(activationTokenExpiryDate)) {
            logger.error("Activation token has expired for user: {}", userEntity.getEmail());
            throw new IllegalArgumentException("Activation token has expired");
        }

        userEntity.setEnabled(true);
        userEntity.setActivationToken(null);
        userEntity.setActivationTokenExpiration(null);
        userRepository.save(userEntity);

        logger.info("Account activated successfully for user: {}", userEntity.getEmail());
    }
}
