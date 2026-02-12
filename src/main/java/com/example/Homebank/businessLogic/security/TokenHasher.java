package com.example.Homebank.businessLogic.security;


import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Utility class for hashing tokens using SHA-256.
 */
public class TokenHasher {
    /**
     * Hashes the given token using SHA-256 and returns the hexadecimal representation of the hash.
     *
     * @param token The token to be hashed.
     * @return The SHA-256 hash of the token as a hexadecimal string.
     */
    public static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
