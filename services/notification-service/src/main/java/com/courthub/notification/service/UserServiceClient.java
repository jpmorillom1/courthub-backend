package com.courthub.notification.service;

import com.courthub.common.dto.UserDto;
import com.courthub.notification.client.UserServiceFeignClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Client for communicating with user-service via OpenFeign.
 * Fetches user data from PostgreSQL via HTTP with service discovery.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceClient {

    private final UserServiceFeignClient userServiceFeignClient;

    public String getUserEmail(UUID userId) {
        try {
            UserDto user = userServiceFeignClient.getUserById(userId);
            return (user != null && user.getEmail() != null) ? user.getEmail() : "customer@courthub.com";
        } catch (FeignException e) {
            log.warn("User service unavailable when fetching email. Using fallback: userId={}", userId, e);
            return "valued.customer@example.com";
        } catch (Exception e) {
            log.warn("Unexpected error while fetching user email. Using fallback: userId={}", userId, e);
            return "valued.customer@example.com";
        }
    }

    public String getUserName(UUID userId) {
        try {
            UserDto user = userServiceFeignClient.getUserById(userId);
            if (user != null && user.getName() != null) {
                return user.getName();
            }
            throw new RuntimeException("User name not found for user ID: " + userId);
        } catch (FeignException e) {
            log.error("Failed to fetch user name from user service for user ID: {}", userId, e);
            throw new RuntimeException("Failed to fetch user data", e);
        } catch (Exception e) {
            log.error("Failed to fetch user name for user ID: {}", userId, e);
            throw new RuntimeException("Failed to fetch user data", e);
        }
    }
}
