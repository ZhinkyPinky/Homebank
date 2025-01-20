package com.example.Homebank.businessLogic.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RefreshJwtUtil extends AbstractJwtUtil {
    @Value("${jwt.refresh.secret}")
    private String REFRESH_SECRET;

    @Value("${jwt.refresh.duration}")
    private String REFRESH_DURATION;

    @Override
    protected String getSecret() {
        return REFRESH_SECRET;
    }

    @Override
    protected long getTokenDuration() {
        return TimeUnit.HOURS.toMillis(Long.parseLong(REFRESH_DURATION));
    }
}
