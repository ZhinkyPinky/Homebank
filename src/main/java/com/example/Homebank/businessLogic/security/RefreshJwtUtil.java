package com.example.Homebank.businessLogic.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Utility class for generating and validating refresh JWT tokens.
 */
@Component
public class RefreshJwtUtil extends AbstractJwtUtil {
    @Value("${jwt.refresh.secret}")
    private String REFRESH_SECRET;

    @Value("${jwt.refresh.duration}")
    private String REFRESH_DURATION;

    /**
     * Retrieves the secret used to generate the refresh tokens.
     *
     * @return The secret used to generate the signing key for the refresh tokens.
     */
    @Override
    protected String getSecret() {
        return REFRESH_SECRET;
    }


    /**
     * Retrieves the duration of the refresh tokens.
     *
     * @return Refresh token duration.
     */
    @Override
    protected long getTokenDuration() {
        return TimeUnit.DAYS.toMillis(Long.parseLong(REFRESH_DURATION));
    }
}
