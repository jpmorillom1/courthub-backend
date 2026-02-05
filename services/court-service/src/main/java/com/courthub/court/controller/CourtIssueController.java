package com.courthub.court.controller;

import com.courthub.court.config.JwtAuthenticationToken;
import com.courthub.court.domain.IssueSeverity;
import com.courthub.court.dto.CourtIssueRequestDto;
import com.courthub.court.dto.CourtIssueResponseDto;
import com.courthub.court.dto.IssueStatusUpdateDto;
import com.courthub.court.service.CourtIssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/courts")
@Tag(name = "Court Issues", description = "Court issues and incident management endpoints.")
public class CourtIssueController {

    private final CourtIssueService issueService;

    public CourtIssueController(CourtIssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping("/issues/severity-levels")
    @Operation(summary = "Get severity levels", description = "Returns a list of all available severity levels for issues.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of severity levels returned")
    })
    public ResponseEntity<List<IssueSeverity>> getSeverityLevels() {
        log.info("Get issue severity levels request received");
        return ResponseEntity.ok(Arrays.asList(IssueSeverity.values()));
    }


    @PostMapping("/{courtId}/issues")
    @Operation(summary = "Report court issue", description = "Report a new issue or incident for a specific court.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Issue reported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "404", description = "Court not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<CourtIssueResponseDto> reportIssue(
            @PathVariable UUID courtId,
            @Valid @RequestBody CourtIssueRequestDto request,
            Authentication authentication) {

        UUID userId = extractUserId(authentication);
        log.info("Report court issue request received: courtId={}, reporterId={}, severity={}",
            courtId, userId, request.getSeverity());

        request.setCourtId(courtId);

        CourtIssueResponseDto issue = issueService.createIssue(request, userId);
        log.info("Court issue created successfully: issueId={}, courtId={}", issue.getId(), courtId);
        return ResponseEntity.status(HttpStatus.CREATED).body(issue);
    }


    @GetMapping("/{courtId}/issues")
    @Operation(summary = "List court issues", description = "Get all issues reported for a specific court.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of issues returned"),
            @ApiResponse(responseCode = "404", description = "Court not found")
    })
    public ResponseEntity<List<CourtIssueResponseDto>> getCourtIssues(@PathVariable UUID courtId) {
        log.info("Get court issues request received: courtId={}", courtId);
        List<CourtIssueResponseDto> issues = issueService.getIssuesByCourtId(courtId);
        log.info("Court issues returned: courtId={}, count={}", courtId, issues.size());
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/issues/{issueId}")
    @Operation(summary = "Get issue details", description = "Get detailed information about a specific issue.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Issue details returned"),
            @ApiResponse(responseCode = "404", description = "Issue not found")
    })
    public ResponseEntity<CourtIssueResponseDto> getIssueById(@PathVariable UUID issueId) {
        log.info("Get issue by id request received: issueId={}", issueId);
        CourtIssueResponseDto issue = issueService.getIssueById(issueId);
        log.info("Issue returned successfully: issueId={}", issueId);
        return ResponseEntity.ok(issue);
    }


    @GetMapping("/issues")
    @Operation(summary = "List all pending issues", description = "Get all pending issues in the system. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of pending issues returned"),
            @ApiResponse(responseCode = "403", description = "Access denied - admin only")
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<CourtIssueResponseDto>> getAllPendingIssues() {
        log.info("Get all pending issues request received");
        List<CourtIssueResponseDto> issues = issueService.getAllPendingIssues();
        log.info("Pending issues returned: count={}", issues.size());
        return ResponseEntity.ok(issues);
    }


    @PatchMapping("/issues/{issueId}/status")
    @Operation(summary = "Update issue status", description = "Update the status of an issue. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status or status transition"),
            @ApiResponse(responseCode = "404", description = "Issue not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - admin only")
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<CourtIssueResponseDto> updateIssueStatus(
            @PathVariable UUID issueId,
            @Valid @RequestBody IssueStatusUpdateDto statusUpdate) {

        log.info("Update issue status request received: issueId={}, status={}", issueId, statusUpdate.getStatus());
        CourtIssueResponseDto updatedIssue = issueService.updateIssueStatus(issueId, statusUpdate);
        log.info("Issue status updated successfully: issueId={}, status={}", issueId, updatedIssue.getStatus());
        return ResponseEntity.ok(updatedIssue);
    }


    private UUID extractUserId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken) {
            return ((JwtAuthenticationToken) authentication).getUserId();
        }
        throw new IllegalStateException("Invalid authentication type");
    }
}
