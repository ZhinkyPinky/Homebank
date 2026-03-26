package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.security.AccessJwtUtil;
import com.example.Homebank.businessLogic.security.RecoveryJwtUtil;
import com.example.Homebank.businessLogic.services.email.EmailService;
import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.repositories.UserRepository;
import com.example.Homebank.presentation.dto.auth.AuthenticationDTO;
import com.example.Homebank.presentation.dto.auth.EmailDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountRecoveryServiceTests {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private RecoveryJwtUtil recoveryJwtUtil;

    @Mock
    private AccessJwtUtil accessJwtUtil;

    @InjectMocks
    private AccountRecoveryService accountRecoveryService;

    @Test
    void initiateAccountRecovery_unknownEmail_returnsWithoutSideEffects() {
        EmailDTO dto = new EmailDTO("missing@example.com");
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> accountRecoveryService.initiateAccountRecovery(dto));

        verify(userRepository).findByEmail("missing@example.com");
        verify(userRepository, never()).save(any(UserEntity.class));
        verifyNoInteractions(passwordEncoder, emailService, recoveryJwtUtil, accessJwtUtil, userService);
    }

    @Test
    void authenticate_unknownEmail_throwsBadCredentials() {
        AuthenticationDTO dto = new AuthenticationDTO("missing@example.com", "secret");
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> accountRecoveryService.authenticate(dto));

        verify(userRepository).findByEmail("missing@example.com");
        verify(userRepository, never()).save(any(UserEntity.class));
        verifyNoInteractions(passwordEncoder, emailService, recoveryJwtUtil, accessJwtUtil, userService);
    }

    @Test
    void authenticate_missingRecoveryState_throwsBadCredentials() {
        AuthenticationDTO dto = new AuthenticationDTO("user@example.com", "secret");
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("user@example.com");
        userEntity.setRecoveryPassword(null);
        userEntity.setRecoveryPasswordExpiration(null);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(userEntity));

        assertThrows(BadCredentialsException.class, () -> accountRecoveryService.authenticate(dto));

        verify(userRepository).findByEmail("user@example.com");
        verify(userRepository, never()).save(any(UserEntity.class));
        verifyNoInteractions(passwordEncoder, emailService, recoveryJwtUtil, accessJwtUtil, userService);
    }
}
