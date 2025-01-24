package com.example.Homebank.businessLogic.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class AccessJwtUtil extends AbstractJwtUtil {
    @Value("${jwt.access.secret}")
    private String ACCESS_SECRET;

    @Value("${jwt.access.duration}")
    private String ACCESS_DURATION;

    @Override
    protected String getSecret() {
        return ACCESS_SECRET;
    }

    @Override
    protected long getTokenDuration() {
        return TimeUnit.MINUTES.toMillis(Long.parseLong(ACCESS_DURATION));
    }
}
