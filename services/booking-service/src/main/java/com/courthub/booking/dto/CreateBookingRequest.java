package com.courthub.booking.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class CreateBookingRequest {

    @NotNull
    private UUID courtId;

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime startTime;

    public UUID getCourtId() {
        return courtId;
    }

    public void setCourtId(UUID courtId) {
        this.courtId = courtId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
}
