package com.courthub.booking.dto;

import com.courthub.booking.domain.BookingStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class BookingResponse {

    private UUID id;
    private UUID timeSlotId;
    private UUID courtId;
    private UUID userId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private BookingStatus status;

    public BookingResponse() {
    }

    public BookingResponse(UUID id, UUID timeSlotId, UUID courtId, UUID userId,
                           LocalDate date, LocalTime startTime, LocalTime endTime, BookingStatus status) {
        this.id = id;
        this.timeSlotId = timeSlotId;
        this.courtId = courtId;
        this.userId = userId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTimeSlotId() {
        return timeSlotId;
    }

    public void setTimeSlotId(UUID timeSlotId) {
        this.timeSlotId = timeSlotId;
    }

    public UUID getCourtId() {
        return courtId;
    }

    public void setCourtId(UUID courtId) {
        this.courtId = courtId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
