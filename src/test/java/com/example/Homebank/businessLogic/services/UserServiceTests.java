package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.security.AuthenticatedUserProvider;
import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.repositories.UserRepository;
import com.example.Homebank.exceptions.validation.PasswordConfirmationMismatchException;
import com.example.Homebank.presentation.dto.auth.ChangePasswordDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;
    @InjectMocks
    private UserService userService;

    @Test
    void changePassword_validPasswords_updatesAuthenticatedUser() {
        UserEntity user = authenticatedUser();
        user.setRefreshToken("existing-session");
        LocalDateTime expiration = LocalDateTime.parse("2026-01-01T00:00:00");
        user.setNextRefreshTokenExpirationDate(expiration);
        when(passwordEncoder.matches("old-password", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        userService.changePassword(new ChangePasswordDTO("old-password", "new-password", "new-password"));

        assertEquals("new-hash", user.getPassword());
        assertEquals("existing-session", user.getRefreshToken());
        assertEquals(expiration, user.getNextRefreshTokenExpirationDate());
        verify(authenticatedUserProvider).getAuthenticatedUser();
        verify(userRepository).save(user);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void changePassword_wrongOldPassword_doesNotSave() {
        UserEntity user = authenticatedUser();
        when(passwordEncoder.matches("wrong-password", "old-hash")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> userService.changePassword(
                new ChangePasswordDTO("wrong-password", "new-password", "new-password")));

        assertEquals("old-hash", user.getPassword());
        verifyNoInteractions(userRepository);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void changePassword_confirmationMismatch_doesNotSave() {
        UserEntity user = authenticatedUser();
        when(passwordEncoder.matches("old-password", "old-hash")).thenReturn(true);

        assertThrows(PasswordConfirmationMismatchException.class, () -> userService.changePassword(
                new ChangePasswordDTO("old-password", "new-password", "different-password")));

        assertEquals("old-hash", user.getPassword());
        verifyNoInteractions(userRepository);
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void changePassword_missingAuthentication_doesNotSave() {
        when(authenticatedUserProvider.getAuthenticatedUser())
                .thenThrow(new AuthenticationCredentialsNotFoundException("Missing authentication"));

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> userService.changePassword(
                new ChangePasswordDTO("old-password", "new-password", "new-password")));

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    private UserEntity authenticatedUser() {
        UserEntity user = new UserEntity();
        user.setId(7);
        user.setEmail("user@example.com");
        user.setPassword("old-hash");
        when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(user);
        return user;
    }
}
