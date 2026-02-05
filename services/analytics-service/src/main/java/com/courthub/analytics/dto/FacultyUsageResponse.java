package com.courthub.analytics.dto;

public record FacultyUsageResponse(
    String faculty,
    int bookingCount,
    double averageOccupancy,
    String color
) {}
