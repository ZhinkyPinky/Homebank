package com.example.Homebank.businessLogic.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class to retrieve information about the currently authenticated user from the security context.
 */
@Component
public class SecurityContextUtility {
    /**
     * Retrieves the authentication object from the security context.
     *
     * @return The authentication object, or null if no authentication is present.
     */
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Retrieves the email of the authenticated user from the security context.
     *
     * @return The email of the authenticated user, or null if no user is authenticated.
     */
    public String getAuthenticatedUserEmail() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }
}
