package com.courthub.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDetailDto {

    private String notificationId;
    private String type;
    private String channel;
    private String message;
    private LocalDateTime timestamp;
    private boolean success;
    private String errorMessage;
}
