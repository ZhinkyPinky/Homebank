package com.example.Homebank.businessLogic.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

public abstract class AbstractJwtUtil {
    private final MacAlgorithm SIG_ALG = Jwts.SIG.HS512;

    /**
     * Generates a JWT.
     *
     * @param username The subject.
     * @return The JWT.
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
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
     * @param user  The subject.
     * @return true if valid, otherwise false.
     */
    public boolean isTokenValid(String token, UserDetails user) {
        return isUsernameCorrect(token, user.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Checks whether the provided username of the subject match the one found in the JWT.
     *
     * @param token    The JWT.
     * @param username The username of the subject
     * @return true if matching, otherwise false.
     */
    private boolean isUsernameCorrect(String token, String username) {
        return extractUsername(token).equals(username);
    }

    /**
     * Extracts the username of the subject from the JWT.
     *
     * @param token The JWT.
     * @return The username of the subject.
     */
    public String extractUsername(String token) {
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
