package com.courthub.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class UserServiceClientConfig {

    @Value("${user-service.url:http://localhost:8081}")
    private String userServiceUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public String userServiceBaseUrl() {
        return userServiceUrl;
    }
}

