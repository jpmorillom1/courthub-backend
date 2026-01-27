package com.courthub.court.dto;

import com.courthub.court.domain.IssueStatus;
import jakarta.validation.constraints.NotNull;

public class IssueStatusUpdateDto {

    @NotNull(message = "Status is required")
    private IssueStatus status;

    public IssueStatusUpdateDto() {
    }

    public IssueStatusUpdateDto(IssueStatus status) {
        this.status = status;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }
}
