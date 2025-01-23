package com.example.Homebank.businessLogic.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public abstract class AbstractJwtUtil {
    private final MacAlgorithm SIG_ALG = Jwts.SIG.HS512;

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(getExpirationDate())
                .signWith(getKey(), SIG_ALG)
                .compact();
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    protected abstract String getSecret();

    private Date getExpirationDate() {
        return new Date(Instant.now().toEpochMilli() + getTokenDuration());
    }

    protected abstract long getTokenDuration();

    public boolean isTokenValid(String token, UserDetails user) {
        return isUsernameCorrect(token, user.getUsername()) && !isTokenExpired(token);
    }

    private boolean isUsernameCorrect(String token, String username) {
        return extractUsername(token).equals(username);
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
    }

}
