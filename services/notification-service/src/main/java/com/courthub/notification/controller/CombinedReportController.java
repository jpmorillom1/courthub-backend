package com.courthub.notification.controller;

import com.courthub.notification.domain.NotificationLog;
import com.courthub.notification.dto.CombinedReportDto;
import com.courthub.notification.dto.NotificationDetailDto;
import com.courthub.notification.repository.NotificationRepository;
import com.courthub.notification.service.UserServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications & Reports", description = "Endpoints for notification history and combined data reports")
public class CombinedReportController {

    private final NotificationRepository notificationRepository;
    private final UserServiceClient userServiceClient;

    @GetMapping("/report/{userId}")
    @Operation(
            summary = "Get combined report (PostgreSQL + MongoDB)",
            description = "Fetches user profile data from PostgreSQL (via user-service) and merges it with notification logs from MongoDB."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Combined report generated successfully"),
            @ApiResponse(description = "User not found or connection error with user-service", responseCode = "404"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CombinedReportDto> getCombinedReport(@PathVariable UUID userId) {
        log.info("Fetching combined report for user ID: {}", userId);

        try {
            String userName = userServiceClient.getUserName(userId);
            String userEmail = userServiceClient.getUserEmail(userId);

            List<NotificationLog> logs = notificationRepository.findByUserIdOrderByTimestampDesc(userId);
            long totalNotifications = notificationRepository.countByUserId(userId);

            List<NotificationDetailDto> notificationDetails = logs.stream()
                    .map(this::mapToDetailDto)
                    .collect(Collectors.toList());

            CombinedReportDto report = CombinedReportDto.builder()
                    .userName(userName)
                    .userEmail(userEmail)
                    .totalNotifications(totalNotifications)
                    .notifications(notificationDetails)
                    .build();

            log.info("Successfully generated combined report for user ID: {}", userId);
            return ResponseEntity.ok(report);

        } catch (Exception e) {
            log.error("Failed to generate combined report for user ID: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get notification logs for a user",
            description = "Returns a list of all notifications sent to a specific user, retrieved from MongoDB."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification history found"),
            @ApiResponse(responseCode = "404", description = "No history found for this user")
    })
    public ResponseEntity<List<NotificationDetailDto>> getUserNotifications(@PathVariable UUID userId) {
        log.info("Fetching notifications for user ID: {}", userId);

        List<NotificationLog> logs = notificationRepository.findByUserIdOrderByTimestampDesc(userId);
        List<NotificationDetailDto> details = logs.stream()
                .map(this::mapToDetailDto)
                .collect(Collectors.toList());

                log.info("Notifications returned for user ID: {} count={}", userId, details.size());

        return ResponseEntity.ok(details);
    }

    private NotificationDetailDto mapToDetailDto(NotificationLog log) {
        return NotificationDetailDto.builder()
                .notificationId(log.getId())
                .type(log.getType().name())
                .channel(log.getChannel().name())
                .message(log.getMessage())
                .timestamp(log.getTimestamp())
                .success(log.isSuccess())
                .errorMessage(log.getErrorMessage())
                .build();
    }
}