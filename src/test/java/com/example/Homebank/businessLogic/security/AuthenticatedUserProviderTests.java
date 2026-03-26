package com.example.Homebank.businessLogic.security;

import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.repositories.UserRepository;
import com.example.Homebank.exceptions.notfound.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticatedUserProviderTests {

    @Mock
    private SecurityContextUtility securityContextUtility;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Test
    void getAuthenticatedUser_returnsUserWhenPresent() {
        UserEntity expectedUser = new UserEntity();
        expectedUser.setEmail("user@example.com");

        when(securityContextUtility.getAuthenticatedUserEmail()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(expectedUser));

        UserEntity result = authenticatedUserProvider.getAuthenticatedUser();

        assertSame(expectedUser, result);
        verify(userRepository).findByEmail("user@example.com");
    }

    @Test
    void getAuthenticatedUser_throwsWhenNoAuthenticatedUserInContext() {
        when(securityContextUtility.getAuthenticatedUserEmail()).thenReturn(null);

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> authenticatedUserProvider.getAuthenticatedUser());

        verify(userRepository, never()).findByEmail(ArgumentMatchers.anyString());
    }

    @Test
    void getAuthenticatedUser_throwsResourceNotFoundWithDetailsWhenUserMissing() {
        when(securityContextUtility.getAuthenticatedUserEmail()).thenReturn("missing@example.com");
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> authenticatedUserProvider.getAuthenticatedUser()
        );

        assertEquals("USER", exception.getResourceType());
        assertNull(exception.getResourceId());
        assertEquals("Authenticated user could not be found.", exception.getMessage());
        assertEquals(0, exception.getMetadata().size());
    }
}
