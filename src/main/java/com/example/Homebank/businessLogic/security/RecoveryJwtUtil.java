package com.example.Homebank.businessLogic.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RecoveryJwtUtil extends AbstractJwtUtil {
    @Value("${jwt.recovery.secret}")
    private String RECOVERY_SECRET;

    @Value("${jwt.recovery.duration}")
    private String RECOVERY_DURATION;

    /**
     * Retrieves the secret used to generate the access tokens.
     *
     * @return The secret used to generate the signing key for the access tokens.
     */
    @Override
    protected String getSecret() {
        return RECOVERY_SECRET;
    }

    /**
     * Retrieves the duration of the access tokens.
     *
     * @return Access token duration.
     */
    @Override
    protected long getTokenDuration() {
        return TimeUnit.MINUTES.toMillis(Long.parseLong(RECOVERY_DURATION));
    }
}
