package com.courthub.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_logs")
public class NotificationLog {

    @Id
    private String id;

    private UUID userId;

    private NotificationType type;

    private NotificationChannel channel;

    private String message;

    private LocalDateTime timestamp;

    private String recipient;

    private String subject;

    private boolean success;

    private String errorMessage;
}
