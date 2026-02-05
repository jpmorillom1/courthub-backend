package com.courthub.analytics.dto;

public record ReservationHistoryResponse(
    String month,
    int completedCount,
    int cancelledCount,
    int year
) {}
