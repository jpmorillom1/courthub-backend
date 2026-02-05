package com.courthub.court.dto;

import com.courthub.court.domain.CourtStatus;
import jakarta.validation.constraints.NotNull;

public class CourtStatusUpdateDto {

    @NotNull
    private CourtStatus status;

    public CourtStatus getStatus() {
        return status;
    }

    public void setStatus(CourtStatus status) {
        this.status = status;
    }
}
