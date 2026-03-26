package com.example.Homebank.businessLogic.security;

import com.example.Homebank.businessLogic.services.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTests {

    @Mock
    private AccessJwtUtil jwtUtil;

    @Mock
    private UserService userService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_returnsTokenExpiredWhenJwtIsExpired() throws Exception {
        MockHttpServletRequest request = bearerRequest("expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.extractEmail("expired-token"))
                .thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"code\":\"TOKEN_EXPIRED\""));
        assertTrue(response.getContentAsString().contains("\"tokenType\":\"ACCESS\""));
        assertTrue(response.getContentAsString().contains("\"path\":\"/customers\""));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_returnsTokenInvalidWhenJwtCannotBeParsed() throws Exception {
        MockHttpServletRequest request = bearerRequest("invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.extractEmail("invalid-token"))
                .thenThrow(new JwtException("Invalid token"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"code\":\"TOKEN_INVALID\""));
        assertTrue(response.getContentAsString().contains("\"tokenType\":\"ACCESS\""));
        assertTrue(response.getContentAsString().contains("\"path\":\"/customers\""));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_returnsTokenInvalidWhenJwtSubjectCannotBeLoaded() throws Exception {
        MockHttpServletRequest request = bearerRequest("subject-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.extractEmail("subject-token")).thenReturn("missing@example.com");
        when(userService.loadUserByUsername("missing@example.com"))
                .thenThrow(new AuthenticationServiceException("User not found"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"code\":\"TOKEN_INVALID\""));
        assertTrue(response.getContentAsString().contains("\"tokenType\":\"ACCESS\""));
        assertTrue(response.getContentAsString().contains("\"path\":\"/customers\""));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_returnsTokenInvalidWhenTokenValidationFails() throws Exception {
        MockHttpServletRequest request = bearerRequest("validation-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails userDetails = userDetails("user@example.com");
        when(jwtUtil.extractEmail("validation-token")).thenReturn("user@example.com");
        when(userService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(jwtUtil.isTokenValid("validation-token", "user@example.com")).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"code\":\"TOKEN_INVALID\""));
        assertTrue(response.getContentAsString().contains("\"tokenType\":\"ACCESS\""));
        assertTrue(response.getContentAsString().contains("\"path\":\"/customers\""));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_setsSecurityContextAndContinuesWhenTokenIsValid() throws Exception {
        MockHttpServletRequest request = bearerRequest("valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails userDetails = userDetails("user@example.com");
        when(jwtUtil.extractEmail("valid-token")).thenReturn("user@example.com");
        when(userService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(jwtUtil.isTokenValid("valid-token", "user@example.com")).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user@example.com", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_skipsTokenValidationForAllPublicEndpoints() throws Exception {
        MockHttpServletRequest[] requests = new MockHttpServletRequest[]{
                new MockHttpServletRequest("POST", "/auth/login"),
                new MockHttpServletRequest("POST", "/auth/refresh"),
                new MockHttpServletRequest("POST", "/auth/register"),
                new MockHttpServletRequest("GET", "/auth/activate"),
                new MockHttpServletRequest("POST", "/account-recovery/initiate"),
                new MockHttpServletRequest("POST", "/account-recovery/authenticate"),
                new MockHttpServletRequest("POST", "/account-recovery/set-new-password")
        };

        for (MockHttpServletRequest request : requests) {
            request.addHeader("Authorization", "Bearer invalid-or-expired-token");
            MockHttpServletResponse response = new MockHttpServletResponse();

            jwtAuthFilter.doFilter(request, response, filterChain);

            assertEquals(200, response.getStatus());
            verify(filterChain).doFilter(request, response);
        }

        verifyNoInteractions(jwtUtil, userService);
    }

    private MockHttpServletRequest bearerRequest(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/customers");
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private UserDetails userDetails(String email) {
        return User.withUsername(email)
                .password("n/a")
                .authorities("ROLE_USER")
                .build();
    }
}
