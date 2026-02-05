package com.courthub.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CombinedReportDto {

    private String userName;
    private String userEmail;
    private long totalNotifications;
    private List<NotificationDetailDto> notifications;
}
