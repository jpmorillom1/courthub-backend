package com.courthub.analytics.dto;

public record StudentRankingResponse(
    String userId,
    String userName,
    String faculty,
    int totalBookings,
    int totalHours,
    String attendanceRate
) {}
