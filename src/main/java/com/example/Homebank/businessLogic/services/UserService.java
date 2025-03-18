package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.security.RefreshJwtUtil;
import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.repositories.UserRepository;
import com.example.Homebank.presentation.controllers.UserController;
import com.example.Homebank.presentation.dto.ChangePasswordDTO;
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

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RefreshJwtUtil refreshJwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(() -> {
            logger.error("User with username: {} not found.", username);
            return new UsernameNotFoundException("User not found");
        });
    }

    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        String refreshToken = changePasswordDTO.refreshToken();
        String username = refreshJwtUtil.extractUsername(refreshToken);

        UserEntity userEntity = (UserEntity) loadUserByUsername(username);

        if (!refreshJwtUtil.isTokenValid(refreshToken, userEntity)) {
            logger.error("Invalid refresh token: {}", refreshToken);
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String oldPassword = changePasswordDTO.oldPassword();
        if (!passwordEncoder.matches(oldPassword, userEntity.getPassword())) {
            logger.error("Invalid password.");
            throw new BadCredentialsException("Invalid password");
        }


        String newPassword = changePasswordDTO.newPassword();
        String confirmNewPassword = changePasswordDTO.confirmNewPassword();
        if (!newPassword.equals(confirmNewPassword)) {
            throw new BadCredentialsException("Passwords do not match");
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        userEntity.setPassword(encodedNewPassword);
        userRepository.save(userEntity);

        logger.info("Password changed successfully for user: {}", username);
    }
}
