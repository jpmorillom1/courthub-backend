package com.courthub.court.dto;

import java.time.LocalTime;
import java.util.UUID;

public class CourtScheduleResponseDto {

    private UUID id;
    private UUID courtId;
    private int dayOfWeek;
    private LocalTime openTime;
    private LocalTime closeTime;

    public CourtScheduleResponseDto() {
    }

    public CourtScheduleResponseDto(UUID id, UUID courtId, int dayOfWeek, LocalTime openTime, LocalTime closeTime) {
        this.id = id;
        this.courtId = courtId;
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCourtId() {
        return courtId;
    }

    public void setCourtId(UUID courtId) {
        this.courtId = courtId;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }
}
