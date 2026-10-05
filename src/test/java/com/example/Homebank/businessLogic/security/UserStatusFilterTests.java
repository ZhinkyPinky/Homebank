package com.example.Homebank.businessLogic.security;

import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.entities.UserStatus;
import com.example.Homebank.businessLogic.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import jakarta.servlet.FilterChain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserStatusFilterTests {

    @Mock
    private UserService userService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private UserStatusFilter userStatusFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/api", "/bank"})
    void doFilterInternal_blocksPendingUserOnProtectedEndpoint(String contextPath) throws Exception {
        setAuthentication("pending@example.com");
        when(userService.loadUserByUsername("pending@example.com")).thenReturn(userWithStatus(UserStatus.ACTIVATION_PENDING));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", contextPath + "/customers");
        request.setContextPath(contextPath);
        MockHttpServletResponse response = new MockHttpServletResponse();

        userStatusFilter.doFilterInternal(request, response, filterChain);

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"code\":\"ACCOUNT_NOT_ACTIVATED\""));
        assertTrue(response.getContentAsString().contains("\"path\":\"" + contextPath + "/customers\""));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_returnsAuthenticationFailedWhenAuthenticatedPrincipalCannotBeLoaded() throws Exception {
        setAuthentication("missing@example.com");
        when(userService.loadUserByUsername("missing@example.com")).thenThrow(new UsernameNotFoundException("User not found"));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/customers");
        MockHttpServletResponse response = new MockHttpServletResponse();

        userStatusFilter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"code\":\"AUTHENTICATION_FAILED\""));
        assertTrue(response.getContentAsString().contains("\"path\":\"/customers\""));
        verify(filterChain, never()).doFilter(request, response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/api", "/bank"})
    void doFilterInternal_allowsPendingUserOnWhitelistedEndpoint(String contextPath) throws Exception {
        setAuthentication("pending@example.com");
        when(userService.loadUserByUsername("pending@example.com")).thenReturn(userWithStatus(UserStatus.ACTIVATION_PENDING));

        for (String endpoint : new String[]{"/auth/activate", "/auth/resend-activation", "/auth/logout"}) {
            MockHttpServletRequest request = new MockHttpServletRequest(
                    endpoint.equals("/auth/activate") ? "GET" : "POST", contextPath + endpoint);
            request.setContextPath(contextPath);
            MockHttpServletResponse response = new MockHttpServletResponse();

            userStatusFilter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertEquals(200, response.getStatus());
        }
    }

    @Test
    void doFilterInternal_allowsActiveUser() throws Exception {
        setAuthentication("active@example.com");
        when(userService.loadUserByUsername("active@example.com")).thenReturn(userWithStatus(UserStatus.ACTIVE));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/customers");
        MockHttpServletResponse response = new MockHttpServletResponse();

        userStatusFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void doFilterInternal_allowsWhenUnauthenticated() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/customers");
        MockHttpServletResponse response = new MockHttpServletResponse();

        userStatusFilter.doFilterInternal(request, response, filterChain);

        verify(userService, never()).loadUserByUsername(org.mockito.ArgumentMatchers.anyString());
        verify(filterChain).doFilter(request, response);
    }

    private void setAuthentication(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, "n/a", java.util.List.of())
        );
    }

    private UserEntity userWithStatus(UserStatus status) {
        UserEntity user = new UserEntity();
        user.setStatus(status);
        user.setEmail(status.name().toLowerCase() + "@example.com");
        return user;
    }
}
