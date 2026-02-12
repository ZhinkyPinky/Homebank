package com.example.Homebank.businessLogic.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for generating opaque tokens.
 */
public class OpaqueTokenGenerator {
    private static final Logger logger = LoggerFactory.getLogger(OpaqueTokenGenerator.class);

    private static final int TOKEN_BYTE_SIZE = 32;

    /**
     * Generates a secure random opaque token encoded in URL-safe Base64 format without padding.
     *
     * @return A securely generated opaque token as a String.
     */
    public static String generateToken() {
        logger.info("Generating a new opaque token.");

        byte[] randomBytes = new byte[TOKEN_BYTE_SIZE];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
