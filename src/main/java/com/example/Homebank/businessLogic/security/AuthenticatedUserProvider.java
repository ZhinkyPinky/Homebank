package com.example.Homebank.businessLogic.security;

import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.repositories.UserRepository;
import com.example.Homebank.exceptions.notfound.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Provides functionality to retrieve the currently authenticated user from the security context.
 */
@Component
@RequiredArgsConstructor
public class AuthenticatedUserProvider {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticatedUserProvider.class);

    private final SecurityContextUtility securityContextUtility;
    private final UserRepository userRepository;

    /**
     * Retrieves the currently authenticated user.
     *
     * @return The authenticated user.
     */
    public UserEntity getAuthenticatedUser() {
        logger.debug("Attempting to retrieve authenticated user.");

        String username = securityContextUtility.getAuthenticatedUserEmail();
        if (username == null) {
            logger.warn("No authenticated user found in security context.");
            throw new AuthenticationCredentialsNotFoundException("No authenticated user in security context.");
        }

        logger.debug("Authenticated user email: {}", username);
        return userRepository.findByEmail(username).orElseThrow(() -> {
                    logger.error("Authenticated user with email '{}' not found in database.", username);
                    return new ResourceNotFoundException(
                            "USER",
                            null,
                            "Authenticated user could not be found."
                    );
                }
        );
    }
}
