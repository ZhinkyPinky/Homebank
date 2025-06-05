package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.email.EmailService;
import com.example.Homebank.businessLogic.security.RecoveryJwtUtil;
import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.repositories.UserRepository;
import com.example.Homebank.presentation.dto.AuthenticationDTO;
import com.example.Homebank.presentation.dto.EmailDTO;
import com.example.Homebank.presentation.dto.RecoveryTokenDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AccountRecoveryService {
    private static final Logger logger = LoggerFactory.getLogger(AccountRecoveryService.class);

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RecoveryJwtUtil recoveryJwtUtil;

    /**
     * Loads a user based on their e-mail.
     *
     * @param username E-mail of user to load.
     * @return A UserDetail implementation.
     */
    @Transactional(readOnly = true)
    public UserEntity loadUserByEmail(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(() -> {
            logger.error("User with e-mail: {} not found.", username);
            return new UsernameNotFoundException("User not found");
        });
    }

    /**
     * If a user with the provided e-mail exists, a recovery password is generated, saved to the DB, and sent to
     * their e-mail address.
     *
     * @param emailDTO E-mail of a user that wants to generate a recovery password.
     */
    @Transactional
    public void initiateAccountRecovery(EmailDTO emailDTO) {
        String email = emailDTO.email();

        logger.info("Generating recovery password for user with e-mail: {}", email);

        UserEntity user = loadUserByEmail(email);

        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[10];
        random.nextBytes(tokenBytes);
        String recoveryPassword = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        String encodedRecoveryPassword = passwordEncoder.encode(recoveryPassword);
        LocalDateTime recoveryPasswordExpirationDate = LocalDateTime.now().plusMinutes(10);

        user.setRecoveryPassword(encodedRecoveryPassword);
        user.setRecoveryPasswordExpiration(recoveryPasswordExpirationDate);
        userRepository.save(user);

        emailService.sendRecoveryPasswordEmail(email, recoveryPassword);

        logger.debug("Recovery password generated for user with e-mail: {}", email);
    }

    /**
     * Authenticates a user based on provided recovery details. If authentication is successful, any saved recovery details
     * for the user are deleted, and a recovery token is generated and returned.
     *
     * @param authenticationDTO E-mail and recovery password.
     * @return Recovery token.
     */
    @Transactional
    public RecoveryTokenDTO authenticate(AuthenticationDTO authenticationDTO) {
        logger.info("Attempting to authenticate user: {}", authenticationDTO.username());

        String username = authenticationDTO.username();
        String providedRecoveryPassword = authenticationDTO.password();

        UserEntity userEntity = loadUserByEmail(username);
        String encodedRecoveryPassword = userEntity.getRecoveryPassword();
        LocalDateTime recoveryPasswordExpirationDate = userEntity.getRecoveryPasswordExpiration();

        if (recoveryPasswordExpirationDate.isAfter(LocalDateTime.now())) {
            logger.error("Authentication failed due to the recovery password having expired.");
            throw new BadCredentialsException("Recovery password has expired");
        }

        if (!passwordEncoder.matches(providedRecoveryPassword, encodedRecoveryPassword)) {
            logger.error("Authentication failed due to the provided recovery password not matching the existing one.");
            throw new BadCredentialsException("Bad credentials");
        }

        userEntity.setRecoveryPassword(null);
        userEntity.setRecoveryPasswordExpiration(null);
        userRepository.save(userEntity);

        String recoveryToken = recoveryJwtUtil.generateToken(username);

        logger.info("User {} authenticated successfully. Token generated.", userEntity.getUsername());

        return new RecoveryTokenDTO(recoveryToken);
    }

    @Transactional
    public void setNewPassword(EmailDTO emailDTO) {
    }

}
