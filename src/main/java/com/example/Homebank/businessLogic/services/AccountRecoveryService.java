package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.services.email.EmailService;
import com.example.Homebank.businessLogic.security.AccessJwtUtil;
import com.example.Homebank.businessLogic.security.OpaqueTokenGenerator;
import com.example.Homebank.businessLogic.security.RecoveryJwtUtil;
import com.example.Homebank.businessLogic.security.TokenHasher;
import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.repositories.UserRepository;
import com.example.Homebank.exceptions.authentication.InvalidRecoveryTokenException;
import com.example.Homebank.exceptions.validation.PasswordConfirmationMismatchException;
import com.example.Homebank.presentation.dto.auth.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Service responsible for account recovery:
 * recovery password generation, recovery authentication, and password reset.
 */
@Service
@RequiredArgsConstructor
public class AccountRecoveryService {
    private static final Logger logger = LoggerFactory.getLogger(AccountRecoveryService.class);

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RecoveryJwtUtil recoveryJwtUtil;
    private final AccessJwtUtil accessJwtUtil;

    @Value("${auth.refresh-token.duration-days:7}")
    private String refreshTokenDurationDays;

    /**
     * If a user with the provided e-mail exists, a recovery password is generated, saved to the DB, and sent to
     * their e-mail address.
     *
     * @param emailDTO E-mail of a user that wants to generate a recovery password.
     */
    @Transactional
    public void initiateAccountRecovery(EmailDTO emailDTO) {
        String email = emailDTO.email();

        logger.info("Processing account recovery initiation request.");

        UserEntity user = userRepository.findByEmail(email.toLowerCase()).orElse(null);
        if (user == null) {
            logger.debug("Account recovery initiation processed.");
            return;
        }

        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[10];
        random.nextBytes(tokenBytes);
        String recoveryPassword = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        String encodedRecoveryPassword = passwordEncoder.encode(recoveryPassword);
        LocalDateTime recoveryPasswordExpirationDate = LocalDateTime.now().plusMinutes(10);

        user.setRecoveryPassword(encodedRecoveryPassword);
        user.setRecoveryPasswordExpiration(recoveryPasswordExpirationDate);
        userRepository.save(user);

        emailService.sendEmail(email, "Homebank - Recovery password", recoveryPassword);

        logger.debug("Account recovery initiation processed.");
    }

    /**
     * Authenticates a user based on provided recovery details. If authentication is successful, any saved recovery details
     * for the user are deleted, and a recovery token is generated and returned.
     * Authentication failures are reported as bad credentials.
     *
     * @param authenticationDTO E-mail and recovery password.
     * @return Recovery token.
     */
    @Transactional
    public RecoveryTokenDTO authenticate(AuthenticationDTO authenticationDTO) {
        logger.info("Attempting account recovery authentication.");

        String email = authenticationDTO.email();
        String providedRecoveryPassword = authenticationDTO.password();

        UserEntity userEntity = userRepository.findByEmail(email.toLowerCase()).orElse(null);
        if (userEntity == null) {
            logger.info("Account recovery authentication failed.");
            throw new BadCredentialsException("Bad credentials");
        }

        String encodedRecoveryPassword = userEntity.getRecoveryPassword();
        LocalDateTime recoveryPasswordExpirationDate = userEntity.getRecoveryPasswordExpiration();

        if (encodedRecoveryPassword == null
                || recoveryPasswordExpirationDate == null
                || recoveryPasswordExpirationDate.isBefore(LocalDateTime.now())) {
            logger.info("Account recovery authentication failed.");
            throw new BadCredentialsException("Recovery password has expired");
        }

        if (!passwordEncoder.matches(providedRecoveryPassword, encodedRecoveryPassword)) {
            logger.info("Account recovery authentication failed.");
            throw new BadCredentialsException("Bad credentials");
        }

        userEntity.setRecoveryPassword(null);
        userEntity.setRecoveryPasswordExpiration(null);
        userRepository.save(userEntity);

        String recoveryToken = recoveryJwtUtil.generateToken(email);

        logger.info("Account recovery authentication succeeded.");

        return new RecoveryTokenDTO(recoveryToken);
    }

    /**
     * Updates the user's password and refresh token after validating the recovery token and provided passwords.
     *
     * @param setNewPasswordDTO A DTO containing the recovery token, new password, and confirmation of the new password.
     *                          The recovery token is used to validate the request.
     *                          The new password and confirm new password must match to proceed.
     * @return A DTO containing the new access token, new refresh token, and a success message if the update is successful.
     * Throws an exception if the recovery token is invalid or if the provided passwords do not match.
     */
    @Transactional
    public AccessAndRefreshTokenDTO setNewPassword(SetNewPasswordDTO setNewPasswordDTO) {
        logger.info("Attempting to change password.");

        String recoveryToken = setNewPasswordDTO.recoveryToken();
        String newPassword = setNewPasswordDTO.newPassword();
        String confirmNewPassword = setNewPasswordDTO.confirmNewPassword();

        if (!newPassword.equals(confirmNewPassword)) {
            logger.error("Changing password failed due to the new password not matching the confirm new password.");
            throw new PasswordConfirmationMismatchException();
        }

        String email = recoveryJwtUtil.extractEmail(recoveryToken);
        UserEntity userEntity = (UserEntity) userService.loadUserByUsername(email);

        if (!recoveryJwtUtil.isTokenValid(recoveryToken, userEntity.getEmail())) {
            logger.error("Changing password failed due to an invalid recovery token: {}", recoveryToken);
            throw new InvalidRecoveryTokenException();
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);

        String accessToken = accessJwtUtil.generateToken(userEntity.getUsername());
        String refreshToken = OpaqueTokenGenerator.generateToken();
        String hashedRefreshToken = TokenHasher.hash(refreshToken);
        LocalDateTime refreshTokenExpirationDate = LocalDateTime.now().plusDays(Long.parseLong(refreshTokenDurationDays));

        userEntity.setPassword(encodedNewPassword);
        userEntity.setRefreshToken(hashedRefreshToken);
        userEntity.setNextRefreshTokenExpirationDate(refreshTokenExpirationDate);
        userRepository.save(userEntity);

        logger.debug("Password changed successfully for user: {}", email);

        return new AccessAndRefreshTokenDTO(accessToken, refreshToken, "Password changed successfully", userEntity.getStatus().toString());
    }

}
