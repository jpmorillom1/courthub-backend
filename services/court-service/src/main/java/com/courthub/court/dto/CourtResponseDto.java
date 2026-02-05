package com.courthub.court.dto;

import com.courthub.court.domain.CourtStatus;
import com.courthub.court.domain.SportType;
import com.courthub.court.domain.SurfaceType;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CourtResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private String name;
    private String location;
    private SportType sportType;
    private SurfaceType surfaceType;
    private int capacity;
    private CourtStatus status;
    private Instant createdAt;
    private String videoUrl;
    private List<CourtScheduleResponseDto> schedules;

    public CourtResponseDto() {
    }

    public CourtResponseDto(UUID id, String name, String location, SportType sportType, SurfaceType surfaceType,
                            int capacity, CourtStatus status, Instant createdAt, String videoUrl, List<CourtScheduleResponseDto> schedules) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.sportType = sportType;
        this.surfaceType = surfaceType;
        this.capacity = capacity;
        this.status = status;
        this.createdAt = createdAt;
        this.videoUrl = videoUrl;
        this.schedules = schedules;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public List<CourtScheduleResponseDto> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<CourtScheduleResponseDto> schedules) {
        this.schedules = schedules;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
