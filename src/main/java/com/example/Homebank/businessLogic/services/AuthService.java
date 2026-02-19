package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.security.OpaqueTokenGenerator;
import com.example.Homebank.businessLogic.security.TokenHasher;
import com.example.Homebank.businessLogic.services.email.EmailService;
import com.example.Homebank.businessLogic.security.AccessJwtUtil;
import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.entities.UserStatus;
import com.example.Homebank.dataAccess.repositories.UserRepository;
import com.example.Homebank.exceptions.authentication.AccountNotActivatedException;
import com.example.Homebank.presentation.ApiPaths;
import com.example.Homebank.presentation.dto.*;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

/**
 * Service for handling authentication and registration of users. Also handles refreshing of tokens and signing out.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    private final AccessJwtUtil accessJwtUtil;

    private final UserRepository userRepository;
    private final UserService userService;
    private final EmailService emailService;

    // Duration in days.
    @Value("${jwt.refresh.duration:7}")
    private String refreshTokenDurationDays;

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

        if (userEntity == null) {
            logger.error("Authentication failed for user: {}. User not found.", email);
            throw new BadCredentialsException("Wrong email or password");
        }

        //TODO: authentication manager should already pick up on this?
        if (!userEntity.isEnabled()) {
            logger.error("User {} attempted to authenticate but account is not activated.", email);
            throw new DisabledException("Account is disabled");
        }

        String accessToken = accessJwtUtil.generateToken(userEntity.getUsername());
        String refreshToken = OpaqueTokenGenerator.generateToken();
        String hashedRefreshToken = TokenHasher.hash(refreshToken);
        LocalDateTime refreshTokenExpirationDate = LocalDateTime.now().plusDays(Long.parseLong(refreshTokenDurationDays));

        userEntity.setRefreshToken(hashedRefreshToken);
        userEntity.setNextRefreshTokenExpirationDate(refreshTokenExpirationDate);
        userRepository.save(userEntity);

        logger.info("User {} authenticated successfully. Tokens generated.", userEntity.getUsername());

        return new AccessAndRefreshTokenDTO(accessToken, refreshToken, "Login successful", userEntity.getStatus().toString());
    }

    /**
     * Registers a new user.
     *
     * @param registrationDTO Email and password.
     */
    @Transactional
    public void register(RegistrationDTO registrationDTO) {
        logger.info("Attempting to register new user: {}", registrationDTO.email());

        if (userRepository.findByEmail(registrationDTO.email()).isPresent()) {
            logger.error("Email {} already exists", registrationDTO.email());
            throw new EntityExistsException("Email already exists");
        }

        String email = registrationDTO.email();
        String password = registrationDTO.password();
        String encodedPassword = passwordEncoder.encode(password);

        String refreshToken = OpaqueTokenGenerator.generateToken();
        String hashedRefreshToken = TokenHasher.hash(refreshToken);
        LocalDateTime refreshTokenExpirationDate = LocalDateTime.now().plusDays(Long.parseLong(refreshTokenDurationDays));

        String activationToken = OpaqueTokenGenerator.generateToken();
        String hashedActivationToken = TokenHasher.hash(activationToken);
        LocalDateTime activationTokenExpirationDate = LocalDateTime.now().plusHours(activationTokenDurationHours);


        LocalDateTime currentDateTime = LocalDateTime.now();

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(email);
        userEntity.setPassword(encodedPassword);
        userEntity.setRefreshToken(hashedRefreshToken);
        userEntity.setNextRefreshTokenExpirationDate(refreshTokenExpirationDate);
        userEntity.setActivationToken(hashedActivationToken);
        userEntity.setActivationTokenExpirationDate(activationTokenExpirationDate);
        userEntity.setRowCreatedDate(currentDateTime);
        userEntity.setRowLastEditDate(currentDateTime);
        userEntity.setRowVersion(currentDateTime);

        userRepository.save(userEntity);

        logger.info("User {} registered successfully. Tokens generated.", email);

        sendActivationEmail(email, activationToken);
    }

    /**
     * Sends an activation email to the user with a link containing the activation token.
     *
     * @param email           The email address of the user to send the activation email to.
     * @param activationToken The activation token to include in the activation link.
     */
    private void sendActivationEmail(String email, String activationToken) {
        String activationLink = applicationURL + ApiPaths.AUTH + ApiPaths.ACTIVATE + "?token=" + activationToken;
        emailService.sendEmail(email, "Activate your account", "Please click the following link to activate your account: " + activationLink);
    }

    /**
     * Resends the activation email to the user if the account is not activated. Generates a new activation token and updates it in the DB.
     *
     * @param email E-mail extracted from authenticated principal.
     */
    @Transactional
    public void resendActivationEmail(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        logger.info("Processing resend activation email request.");

        Optional<UserEntity> optionalUserEntity = userRepository.findByEmail(normalizedEmail);
        if (optionalUserEntity.isEmpty()) {
            logger.info("Resend activation email request completed.");
            return;
        }

        UserEntity userEntity = optionalUserEntity.get();

        if (userEntity.getStatus() != UserStatus.ACTIVATION_PENDING) {
            logger.info("Resend activation email request completed.");
            return;
        }

        String activationToken = OpaqueTokenGenerator.generateToken();
        String hashedActivationToken = TokenHasher.hash(activationToken);
        LocalDateTime activationTokenExpirationDate = LocalDateTime.now().plusHours(activationTokenDurationHours);

        userEntity.setActivationToken(hashedActivationToken);
        userEntity.setActivationTokenExpirationDate(activationTokenExpirationDate);
        userRepository.save(userEntity);

        logger.info("Resend activation email request completed.");

        sendActivationEmail(normalizedEmail, activationToken);
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
        String hashedRefreshToken = TokenHasher.hash(refreshToken);

        UserEntity userEntity = userRepository.findByRefreshToken(hashedRefreshToken).orElseThrow(() -> {
            logger.error("Invalid refresh token.");
            return new IllegalArgumentException("Invalid refresh token");
        });

        if (userEntity.getNextRefreshTokenExpirationDate() == null || userEntity.getNextRefreshTokenExpirationDate().isBefore(LocalDateTime.now())) {
            logger.error("Refresh token has expired for user: {}", userEntity.getEmail());

            userEntity.setRefreshToken(null);
            userEntity.setNextRefreshTokenExpirationDate(LocalDateTime.MIN);
            userRepository.save(userEntity);

            throw new IllegalArgumentException("Refresh token has expired");
        }

        if (userEntity.getStatus() != UserStatus.ACTIVE) {
            logger.error("Refresh token rejected for non-active account: {}", userEntity.getEmail());
            throw new AccountNotActivatedException();
        }

        String email = userEntity.getEmail();

        logger.info("Attempting to refresh tokens for user: {}", email);

        String newAccessToken = accessJwtUtil.generateToken(email);
        String newRefreshToken = OpaqueTokenGenerator.generateToken();
        String hashedNewRefreshToken = TokenHasher.hash(newRefreshToken);
        LocalDateTime newRefreshTokenExpirationDate = LocalDateTime.now().plusDays(Long.parseLong(refreshTokenDurationDays));

        userEntity.setRefreshToken(hashedNewRefreshToken);
        userEntity.setNextRefreshTokenExpirationDate(newRefreshTokenExpirationDate);
        userRepository.save(userEntity);

        logger.info("Tokens refreshed successfully for user: {}", email);

        return new AccessAndRefreshTokenDTO(newAccessToken, newRefreshToken, "Tokens refreshed", userEntity.getStatus().toString());
    }

    /**
     * Sign out a user by removing the saved refresh token from the DB.
     *
     * @param refreshToken Provided refresh token.
     */
    @Transactional
    public void signOut(String refreshToken) {
        String hashedRefreshToken = TokenHasher.hash(refreshToken);

        UserEntity userEntity = userRepository.findByRefreshToken(hashedRefreshToken).orElseThrow(() -> {
            logger.error("Invalid refresh token provided for sign out.");
            return new IllegalArgumentException("Invalid refresh token");
        });

        String email = userEntity.getEmail();

        logger.info("Attempting to sign out user: {}", email);

        userEntity.setRefreshToken(null);
        userEntity.setNextRefreshTokenExpirationDate(LocalDateTime.MIN);
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

        LocalDateTime activationTokenExpiryDate = userEntity.getActivationTokenExpirationDate();
        if (activationTokenExpiryDate == null) {
            logger.error("Activation token expiration is missing for user: {}", userEntity.getEmail());
            throw new IllegalArgumentException("Invalid activation token");
        }
        if (LocalDateTime.now().isAfter(activationTokenExpiryDate)) {
            logger.error("Activation token has expired for user: {}", userEntity.getEmail());
            throw new IllegalArgumentException("Activation token has expired");
        }

        userEntity.setStatus(UserStatus.ACTIVE);
        userEntity.setActivationToken(null);
        userEntity.setActivationTokenExpirationDate(null);
        userRepository.save(userEntity);

        logger.info("Account activated successfully for user: {}", userEntity.getEmail());
    }
}
