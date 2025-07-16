package com.example.Homebank.businessLogic.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

public abstract class AbstractJwtUtil {
    private final MacAlgorithm SIG_ALG = Jwts.SIG.HS512;

    /**
     * Generates a JWT.
     *
     * @param email The subject.
     * @return The JWT.
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(getExpirationDate())
                .signWith(getKey(), SIG_ALG)
                .compact();
    }

    /**
     * Generate a signing key.
     *
     * @return The signing key.
     */
    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Abstract method meant to retrieve the secret used to generate the signing key.
     *
     * @return The secret.
     */
    protected abstract String getSecret();

    /**
     * @return Date object representing the expiration date of the JWT.
     */
    private Date getExpirationDate() {
        return new Date(Instant.now().toEpochMilli() + getTokenDuration());
    }

    /**
     * Abstract method meant to retrieve the token duration.
     *
     * @return Duration of the token.
     */
    protected abstract long getTokenDuration();

    /**
     * Checks whether the provided JWT is valid.
     *
     * @param token The JWT.
     * @return true if valid, otherwise false.
     */
    public boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }

    /**
     * Checks whether the provided JWT is valid.
     *
     * @param token The JWT.
     * @param email The email of the subject.
     * @return true if valid, otherwise false.
     */
    public boolean isTokenValid(String token, String email) {
        return isEmailCorrect(token, email) && !isTokenExpired(token);
    }

    /**
     * Checks whether the provided email of the subject match the one found in the JWT.
     *
     * @param token    The JWT.
     * @param email The email of the subject
     * @return true if matching, otherwise false.
     */
    private boolean isEmailCorrect(String token, String email) {
        return extractEmail(token).equals(email);
    }

    /**
     * Extracts the email of the subject from the JWT.
     *
     * @param token The JWT.
     * @return The email of the subject.
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extracts the expiration date of the JWT.
     *
     * @param token The JWT.
     * @return The expiration date of the JWT.
     */
    public Date extractExpirationDate(String token) {
        return extractClaims(token).getExpiration();
    }

    /**
     * Checks whether the JWT is expired.
     *
     * @param token The JWT.
     * @return true if expired, otherwise false.
     */
    private boolean isTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date());
    }

    /**
     * Extracts the claims of the JWT.
     *
     * @param token The JWT.
     * @return The JWT claims.
     */
    private Claims extractClaims(String token) {
        return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
    }

}
