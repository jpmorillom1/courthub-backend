package com.courthub.notification.service;

import com.courthub.common.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

/**
 * Client for communicating with user-service.
 * Fetches user data from PostgreSQL via HTTP.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.user-service.url}")
    private String userServiceUrl;


    public String getUserEmail(UUID userId) {
        try {
            String url = userServiceUrl + "/users/" + userId;
            UserDto response = restTemplate.getForObject(url, UserDto.class);
            return (response != null) ? response.getEmail() : "customer@courthub.com";
        } catch (Exception e) {
            log.warn("⚠️ User Service unreachable. Using fallback for ID: {}", userId);
            return "valued.customer@example.com"; // Fallback para que el proceso siga
        }
    }


    public String getUserName(UUID userId) {
        try {
            String url = userServiceUrl + "/users/" + userId;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("name")) {
                return (String) response.get("name");
            }
            
            throw new RuntimeException("User name not found for user ID: " + userId);
        } catch (Exception e) {
            log.error("Failed to fetch user name for user ID: {}", userId, e);
            throw new RuntimeException("Failed to fetch user data", e);
        }
    }
}
