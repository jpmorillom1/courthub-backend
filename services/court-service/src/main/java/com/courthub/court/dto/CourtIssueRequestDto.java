package com.courthub.court.dto;

import com.courthub.court.domain.IssueSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CourtIssueRequestDto {

    @NotNull(message = "Court ID is required")
    private UUID courtId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Severity is required")
    private IssueSeverity severity;

    public CourtIssueRequestDto() {
    }

    public CourtIssueRequestDto(UUID courtId, String title, String description, IssueSeverity severity) {
        this.courtId = courtId;
        this.title = title;
        this.description = description;
        this.severity = severity;
    }

    public UUID getCourtId() {
        return courtId;
    }

    public void setCourtId(UUID courtId) {
        this.courtId = courtId;
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
}
