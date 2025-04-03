package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.email.EmailService;
import com.example.Homebank.businessLogic.security.RefreshJwtUtil;
import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.repositories.UserRepository;
import com.example.Homebank.presentation.dto.ChangePasswordDTO;
import com.example.Homebank.presentation.dto.EmailDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RefreshJwtUtil refreshJwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Loads a user based on their username.
     *
     * @param username Username of user to load.
     * @return A UserDetail implementation.
     */
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Loading user with username: {}", username);

        UserDetails userDetails = userRepository.findByUsername(username).orElseThrow(() -> {
            logger.error("User with username: {} not found.", username);
            return new UsernameNotFoundException("User not found");
        });

        logger.debug("User loaded: {}", userDetails);
        return userDetails;
    }

    /**
     * Change password for a user provided that:
     * 1. Refresh token is valid.
     * 2. The old password match the current one.
     * 3. The new password match the confirmation password.
     *
     * @param changePasswordDTO Refresh token, old password, new password, new password confirmation.
     */
    @Transactional
    public void changePassword(ChangePasswordDTO changePasswordDTO) throws IllegalArgumentException, BadCredentialsException {
        logger.info("Attempting to change password.");
        String refreshToken = changePasswordDTO.refreshToken();
        String username = refreshJwtUtil.extractUsername(refreshToken);

        UserEntity userEntity = (UserEntity) loadUserByUsername(username);

        if (!refreshJwtUtil.isTokenValid(refreshToken, userEntity)) {
            logger.error("Changing password failed due to an invalid refresh token: {}", refreshToken);
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String oldPassword = changePasswordDTO.oldPassword();
        if (!passwordEncoder.matches(oldPassword, userEntity.getPassword())) {
            logger.error("Changing password failed due to the provided old password not matching the existing one.");
            throw new BadCredentialsException("Wrong password");
        }

        String newPassword = changePasswordDTO.newPassword();
        String confirmNewPassword = changePasswordDTO.confirmNewPassword();
        if (!newPassword.equals(confirmNewPassword)) {
            logger.error("Changing password failed due to the new password not matching the confirm new password.");
            throw new BadCredentialsException("Passwords do not match");
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        userEntity.setPassword(encodedNewPassword);
        userRepository.save(userEntity);

        logger.debug("Password changed successfully for user: {}", username);
    }

    /**
     * Initiates account recovery for a user by sending a recovery e-mail to the provided address, if a user with it
     * exists, with a generated recovery token.
     *
     * @param emailDTO E-mail of user that wants to initiate account recovery.
     */
    @Transactional
    public void initiateUserAccountRecovery(EmailDTO emailDTO) {
        String email = emailDTO.email();

        logger.info("Initiating recovery of user with e-mail: {}", email);

        Optional<UserEntity> optionalUserEntity = userRepository.findByEmail(email);
        if (optionalUserEntity.isEmpty()) return; //Return without throwing to not disclose whether e-mail exists.

        UserEntity userEntity = optionalUserEntity.get();

        //TODO: Generate a JWT instead??? Could be easier to store in DB encrypted and still allow finding user.
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[20];
        random.nextBytes(tokenBytes);
        String recoveryToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        userEntity.setRecoveryToken(recoveryToken);
        userRepository.save(userEntity);

        emailService.sendRecoveryEmail(email, recoveryToken);
    }

    /**
     * Generates a new password for the user and sends it to their e-mail address if a user with the recovery token
     * exists.
     *
     * @param recoveryToken Token used to confirm that the user wants their account recovered.
     */
    @Transactional
    public void recoverUserAccount(String recoveryToken) throws EntityNotFoundException {
        logger.info("Recovering user account with recovery token: {}", recoveryToken);

        UserEntity userEntity = userRepository.findByRecoveryToken(recoveryToken).orElseThrow(() -> {
            logger.error("User with recovery token: {} not found.", recoveryToken);
            return new EntityNotFoundException();
        });

        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[20];
        random.nextBytes(tokenBytes);
        String tempPassword = Base64.getEncoder().withoutPadding().encodeToString(tokenBytes);
        String encodedTempPassword = passwordEncoder.encode(tempPassword);

        userEntity.setPassword(encodedTempPassword);
        userEntity.setRecoveryToken(null);
        userRepository.save(userEntity);

        String email = userEntity.getEmail();
        emailService.sendEmail(email, "Homebank - Recovery password", "Here's your recovery password: " + tempPassword + "\nMake sure to change it as soon as possible.");
    }
}
