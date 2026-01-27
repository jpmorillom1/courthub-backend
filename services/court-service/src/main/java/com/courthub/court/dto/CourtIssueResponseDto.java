package com.courthub.court.dto;

import com.courthub.court.domain.IssueSeverity;
import com.courthub.court.domain.IssueStatus;

import java.time.Instant;
import java.util.UUID;

public class CourtIssueResponseDto {

    private UUID id;
    private UUID courtId;
    private UUID reporterId;
    private String title;
    private String description;
    private IssueSeverity severity;
    private IssueStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public CourtIssueResponseDto() {
    }

    public CourtIssueResponseDto(UUID id, UUID courtId, UUID reporterId, String title, String description,
                                 IssueSeverity severity, IssueStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.courtId = courtId;
        this.reporterId = reporterId;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public UUID getReporterId() {
        return reporterId;
    }

    public void setReporterId(UUID reporterId) {
        this.reporterId = reporterId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IssueSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(IssueSeverity severity) {
        this.severity = severity;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
