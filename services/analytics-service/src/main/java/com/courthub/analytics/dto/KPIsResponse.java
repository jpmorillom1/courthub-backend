package com.courthub.analytics.dto;

public record KPIsResponse(
    double occupationRate,
    int totalStudents,
    int maintenanceIssues,
    int criticalIssues,
    int resolvedIssues
) {}
