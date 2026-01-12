package com.courthub.court.event;

import com.courthub.court.domain.CourtStatus;
import com.courthub.court.domain.SportType;
import com.courthub.court.domain.SurfaceType;

import java.time.Instant;
import java.util.UUID;

public class CourtEventPayload {

    private UUID courtId;
    private String name;
    private String location;
    private SportType sportType;
    private SurfaceType surfaceType;
    private int capacity;
    private CourtStatus status;
    private Instant createdAt;

    public CourtEventPayload() {
    }

    public CourtEventPayload(UUID courtId, String name, String location, SportType sportType, SurfaceType surfaceType,
                             int capacity, CourtStatus status, Instant createdAt) {
        this.courtId = courtId;
        this.name = name;
        this.location = location;
        this.sportType = sportType;
        this.surfaceType = surfaceType;
        this.capacity = capacity;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getCourtId() {
        return courtId;
    }

    public void setCourtId(UUID courtId) {
        this.courtId = courtId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
