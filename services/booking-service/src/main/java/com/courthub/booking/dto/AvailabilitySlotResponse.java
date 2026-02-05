//package com.courthub.booking.dto;
//
//
//import com.courthub.common.dto.enums.TimeSlotStatus;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.UUID;
//
//public class AvailabilitySlotResponse {
//
//    private UUID id;
//    private UUID courtId;
//    private LocalDate date;
//    private LocalTime startTime;
//    private LocalTime endTime;
//    private TimeSlotStatus status;
//
//    public AvailabilitySlotResponse() {
//    }
//
//    public AvailabilitySlotResponse(UUID id, UUID courtId, LocalDate date, LocalTime startTime, LocalTime endTime, TimeSlotStatus status) {
//        this.id = id;
//        this.courtId = courtId;
//        this.date = date;
//        this.startTime = startTime;
//        this.endTime = endTime;
//        this.status = status;
//    }
//
//    public UUID getId() {
//        return id;
//    }
//
//    public void setId(UUID id) {
//        this.id = id;
//    }
//
//    public UUID getCourtId() {
//        return courtId;
//    }
//
//    public void setCourtId(UUID courtId) {
//        this.courtId = courtId;
//    }
//
//    public LocalDate getDate() {
//        return date;
//    }
//
//    public void setDate(LocalDate date) {
//        this.date = date;
//    }
//
//    public LocalTime getStartTime() {
//        return startTime;
//    }
//
//    public void setStartTime(LocalTime startTime) {
//        this.startTime = startTime;
//    }
//
//    public LocalTime getEndTime() {
//        return endTime;
//    }
//
//    public void setEndTime(LocalTime endTime) {
//        this.endTime = endTime;
//    }
//
//    public TimeSlotStatus getStatus() {
//        return status;
//    }
//
//    public void setStatus(TimeSlotStatus status) {
//        this.status = status;
//    }
//}
