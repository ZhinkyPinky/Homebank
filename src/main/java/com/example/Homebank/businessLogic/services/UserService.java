package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.email.EmailService;
import com.example.Homebank.businessLogic.security.RefreshJwtUtil;
import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.repositories.UserRepository;
import com.example.Homebank.presentation.dto.ChangePasswordDTO;
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

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RefreshJwtUtil refreshJwtUtil;
    private final PasswordEncoder passwordEncoder;

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
     * 2. The old password matches the current one.
     * 3. The new password matches the confirmation password.
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
}
