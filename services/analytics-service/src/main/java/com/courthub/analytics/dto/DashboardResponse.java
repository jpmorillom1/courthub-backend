package com.courthub.analytics.dto;

import java.util.List;

public record DashboardResponse(
    KPIsResponse kpis,
    HeatmapResponse heatmap,
    List<FacultyUsageResponse> facultyUsage,
    List<StudentRankingResponse> topActiveStudents,
    List<ReservationHistoryResponse> reservationsHistory
) {}
