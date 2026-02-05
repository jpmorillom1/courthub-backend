package com.courthub.user.config;

import com.courthub.common.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration-minutes:15}")
    private long accessTokenExpirationMinutes;

    @Value("${jwt.refresh-token-expiration-days:30}")
    private long refreshTokenExpirationDays;

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        return new JwtTokenProvider(jwtSecret, accessTokenExpirationMinutes, refreshTokenExpirationDays);
    }
}

