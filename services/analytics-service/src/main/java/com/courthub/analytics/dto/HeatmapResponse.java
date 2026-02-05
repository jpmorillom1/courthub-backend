package com.courthub.analytics.dto;

import java.util.Map;

public record HeatmapResponse(
    Map<String, Map<String, Integer>> dayHourMatrix
) {}
