package com.courthub.court.event;

import com.courthub.court.domain.CourtStatus;
import com.courthub.court.domain.SportType;
import com.courthub.court.domain.SurfaceType;

import java.time.LocalTime;
import java.util.UUID;

public class CourtScheduleEventPayload {

    private UUID courtId;
    private int dayOfWeek;
    private LocalTime openTime;
    private LocalTime closeTime;
    private SportType sportType;
    private SurfaceType surfaceType;
    private int capacity;
    private CourtStatus status;

    public CourtScheduleEventPayload() {
    }

    public CourtScheduleEventPayload(UUID courtId, int dayOfWeek, LocalTime openTime, LocalTime closeTime,
                                     SportType sportType, SurfaceType surfaceType, int capacity, CourtStatus status) {
        this.courtId = courtId;
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.sportType = sportType;
        this.surfaceType = surfaceType;
        this.capacity = capacity;
        this.status = status;
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

    public SportType getSportType() {
        return sportType;
    }

    public void setSportType(SportType sportType) {
        this.sportType = sportType;
    }

    public SurfaceType getSurfaceType() {
        return surfaceType;
    }

    public void setSurfaceType(SurfaceType surfaceType) {
        this.surfaceType = surfaceType;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public CourtStatus getStatus() {
        return status;
    }

    public void setStatus(CourtStatus status) {
        this.status = status;
    }
}
