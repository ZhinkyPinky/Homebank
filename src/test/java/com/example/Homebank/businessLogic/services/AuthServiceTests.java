package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.security.AccessJwtUtil;
import com.example.Homebank.businessLogic.security.TokenHasher;
import com.example.Homebank.businessLogic.services.email.EmailService;
import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.entities.UserStatus;
import com.example.Homebank.dataAccess.repositories.UserRepository;
import com.example.Homebank.exceptions.authentication.RefreshTokenExpiredException;
import com.example.Homebank.exceptions.authentication.AccountNotActivatedException;
import com.example.Homebank.presentation.dto.auth.AccessAndRefreshTokenDTO;
import com.example.Homebank.presentation.dto.auth.AuthenticationDTO;
import com.example.Homebank.presentation.dto.auth.RegistrationDTO;
import com.example.Homebank.presentation.dto.auth.RefreshTokenDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccessJwtUtil accessJwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenDurationDays", "7");
        ReflectionTestUtils.setField(authService, "applicationURL", "http://localhost:8080");
        ReflectionTestUtils.setField(authService, "activationTokenDurationHours", 24L);
    }

    @Test
    void register_normalizesEmailForLookupAndPersistence() {
        RegistrationDTO dto = new RegistrationDTO("  Test.User@Example.COM  ", "Password123");
        when(userRepository.findByEmail("test.user@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123")).thenReturn("encoded-password");

        authService.register(dto);

        verify(userRepository).findByEmail("test.user@example.com");

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        UserEntity savedUser = userCaptor.getValue();
        assertEquals("test.user@example.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());

        verify(emailService).sendEmail(
                eq("test.user@example.com"),
                eq("Activate your account"),
                any(String.class)
        );
    }

    @Test
    void register_existingEmail_returnsWithoutSavingOrSendingEmail() {
        RegistrationDTO dto = new RegistrationDTO(" Existing@Example.COM ", "Password123");
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new UserEntity()));

        authService.register(dto);

        verify(userRepository).findByEmail("existing@example.com");
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(passwordEncoder, never()).encode(any(String.class));
        verify(emailService, never()).sendEmail(any(String.class), any(String.class), any(String.class));
    }

    @Test
    void authenticate_activeAccount_normalizesEmailAndPersistsHashedRefreshToken() {
        AuthenticationDTO dto = new AuthenticationDTO("  USER@Example.COM  ", "secret");
        UserEntity user = new UserEntity();
        user.setEmail("user@example.com");
        user.setStatus(UserStatus.ACTIVE);
        user.setEnabled(true);

        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(accessJwtUtil.generateToken("user@example.com")).thenReturn("access-token");

        LocalDateTime beforeAuthentication = LocalDateTime.now();
        AccessAndRefreshTokenDTO result = authService.authenticate(dto);
        LocalDateTime afterAuthentication = LocalDateTime.now();

        ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(tokenCaptor.capture());
        UsernamePasswordAuthenticationToken captured = tokenCaptor.getValue();
        assertEquals("user@example.com", captured.getPrincipal());
        assertEquals("secret", captured.getCredentials());
        assertNotNull(result);
        assertEquals("access-token", result.accessToken());
        assertEquals("ACTIVE", result.accountStatus());
        assertNotNull(result.refreshToken());
        assertTrue(!result.refreshToken().isBlank());
        assertEquals(Duration.ofDays(7), result.refreshTokenDuration());
        assertEquals(TokenHasher.hash(result.refreshToken()), user.getRefreshToken());
        assertTrue(!user.getNextRefreshTokenExpirationDate().isBefore(beforeAuthentication.plusDays(7)));
        assertTrue(!user.getNextRefreshTokenExpirationDate().isAfter(afterAuthentication.plusDays(7)));
        verify(userRepository).save(user);
    }

    @Test
    void authenticate_pendingAccount_returnsAccessTokenAndRevokesStoredRefreshToken() {
        UserEntity user = new UserEntity();
        user.setEmail("pending@example.com");
        user.setStatus(UserStatus.ACTIVATION_PENDING);
        user.setEnabled(true);
        user.setRefreshToken(TokenHasher.hash("old-refresh-token"));
        user.setNextRefreshTokenExpirationDate(LocalDateTime.now().plusDays(7));

        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(accessJwtUtil.generateToken(user.getEmail())).thenReturn("pending-access-token");

        AccessAndRefreshTokenDTO result = authService.authenticate(new AuthenticationDTO(user.getEmail(), "secret"));

        assertEquals("pending-access-token", result.accessToken());
        assertEquals("ACTIVATION_PENDING", result.accountStatus());
        assertNull(result.refreshToken());
        assertNull(result.refreshTokenDuration());
        assertNull(user.getRefreshToken());
        assertEquals(LocalDateTime.of(1970, 1, 1, 0, 0), user.getNextRefreshTokenExpirationDate());
        verify(userRepository).save(user);
    }

    @Test
    void refreshTokens_pendingAccount_rejectsPreviouslyIssuedToken() {
        String refreshToken = "previous-refresh-token";
        UserEntity user = new UserEntity();
        user.setStatus(UserStatus.ACTIVATION_PENDING);
        user.setRefreshToken(TokenHasher.hash(refreshToken));
        user.setNextRefreshTokenExpirationDate(LocalDateTime.now().plusDays(7));
        when(userRepository.findByRefreshToken(user.getRefreshToken())).thenReturn(Optional.of(user));

        assertThrows(AccountNotActivatedException.class,
                () -> authService.refreshTokens(new RefreshTokenDTO(refreshToken)));

        verify(userRepository, never()).save(any());
        verify(accessJwtUtil, never()).generateToken(any());
    }

    @Test
    void refreshTokens_expiredToken_clearsTokenWithDatabaseCompatibleExpiration() {
        String refreshToken = "expired-refresh-token";
        UserEntity user = new UserEntity();
        user.setStatus(UserStatus.ACTIVE);
        user.setRefreshToken(TokenHasher.hash(refreshToken));
        user.setNextRefreshTokenExpirationDate(LocalDateTime.now().minusDays(1));
        when(userRepository.findByRefreshToken(user.getRefreshToken())).thenReturn(Optional.of(user));

        assertThrows(RefreshTokenExpiredException.class,
                () -> authService.refreshTokens(new RefreshTokenDTO(refreshToken)));

        assertNull(user.getRefreshToken());
        assertEquals(LocalDateTime.of(1970, 1, 1, 0, 0), user.getNextRefreshTokenExpirationDate());
        verify(userRepository).save(user);
        verify(accessJwtUtil, never()).generateToken(any());
    }

    @Test
    void signOut_existingRefreshToken_invalidatesStoredSession() {
        String refreshToken = "refresh-token";
        UserEntity user = new UserEntity();
        user.setRefreshToken(TokenHasher.hash(refreshToken));
        user.setNextRefreshTokenExpirationDate(LocalDateTime.now().plusDays(7));
        when(userRepository.findByRefreshToken(TokenHasher.hash(refreshToken))).thenReturn(Optional.of(user));

        authService.signOut(refreshToken);

        assertNull(user.getRefreshToken());
        assertEquals(LocalDateTime.of(1970, 1, 1, 0, 0), user.getNextRefreshTokenExpirationDate());
        verify(userRepository).save(user);
    }

    @Test
    void signOut_unknownRefreshToken_completesWithoutSaving() {
        String refreshToken = "unknown-refresh-token";
        when(userRepository.findByRefreshToken(TokenHasher.hash(refreshToken))).thenReturn(Optional.empty());

        authService.signOut(refreshToken);

        verify(userRepository, never()).save(any(UserEntity.class));
    }
}
