package com.courthub.court.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public class CourtScheduleRequestDto {

    @Min(1)
    @Max(7)
    @Schema(description = "Day of week (1=Monday, 7=Sunday)",
            example = "1",
            minimum = "1",
            maximum = "7")
    private int dayOfWeek;

    @NotNull
    @Schema(description = "Opening time",
            example = "08:00:00",
            type = "string",
            format = "time")
    private LocalTime openTime;

    @NotNull
    @Schema(description = "Closing time",
            example = "20:00:00",
            type = "string",
            format = "time")
    private LocalTime closeTime;

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
