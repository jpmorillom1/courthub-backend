package com.courthub.common.dto.analytics;

import java.time.LocalDate;
import java.time.LocalTime;

public record BookingInternalDTO(
    String id,
    String courtId,
    LocalDate date,
    LocalTime startTime,
    String status,
    String userId
) {}
