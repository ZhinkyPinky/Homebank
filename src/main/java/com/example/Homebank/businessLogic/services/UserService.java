package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.security.AuthenticatedUserProvider;
import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.repositories.UserRepository;
import com.example.Homebank.exceptions.validation.PasswordConfirmationMismatchException;
import com.example.Homebank.presentation.dto.auth.ChangePasswordDTO;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling user-related operations, such as loading user details and changing passwords.
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    /**
     * Loads a user based on their email.
     *
     * @param email Email of user to load.
     * @return The matching {@link UserDetails} instance.
     */
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        logger.info("Loading user with email: {}", email);

        UserDetails userDetails = userRepository.findByEmail(email).orElseThrow(() -> {
            logger.error("User with email: {} not found.", email);
            return new UsernameNotFoundException("User not found");
        });

        logger.debug("User loaded: {}", userDetails);
        return userDetails;
    }

    /**
     * Changes the authenticated user's password when the old password matches and
     * the new password matches its confirmation.
     *
     * @param changePasswordDTO Old password, new password, and new password confirmation.
     */
    @Transactional
    public void changePassword(ChangePasswordDTO changePasswordDTO) throws IllegalArgumentException, BadCredentialsException {
        logger.info("Attempting to change password.");
        UserEntity userEntity = authenticatedUserProvider.getAuthenticatedUser();

        String oldPassword = changePasswordDTO.oldPassword();
        if (!passwordEncoder.matches(oldPassword, userEntity.getPassword())) {
            logger.error("Changing password failed due to the provided old password not matching the existing one.");
            throw new BadCredentialsException("Wrong password");
        }

        String newPassword = changePasswordDTO.newPassword();
        String confirmNewPassword = changePasswordDTO.confirmNewPassword();
        if (!newPassword.equals(confirmNewPassword)) {
            logger.error("Changing password failed due to the new password not matching the confirm new password.");
            throw new PasswordConfirmationMismatchException();
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        userEntity.setPassword(encodedNewPassword);
        userRepository.save(userEntity);

        logger.debug("Password changed successfully for user: {}", userEntity.getEmail());
    }
}
