package com.courthub.realtime.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AvailabilitySlotResponse {

    private UUID id;
    private UUID courtId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private TimeSlotStatus status;

}
