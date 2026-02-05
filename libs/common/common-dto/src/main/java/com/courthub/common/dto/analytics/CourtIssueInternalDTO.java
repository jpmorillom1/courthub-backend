package com.courthub.common.dto.analytics;

import java.time.LocalDateTime;

public record CourtIssueInternalDTO(
    String id,
    String courtId,
    String severity,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
