package com.courthub.court.controller;

import com.courthub.court.domain.CourtStatus;
import com.courthub.court.domain.SportType;
import com.courthub.court.domain.SurfaceType;
import com.courthub.court.dto.CourtRequestDto;
import com.courthub.court.dto.CourtResponseDto;
import com.courthub.court.dto.CourtScheduleRequestDto;
import com.courthub.court.dto.CourtScheduleResponseDto;
import com.courthub.court.dto.CourtStatusUpdateDto;
import com.courthub.court.service.CourtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/courts")
@Tag(name = "Courts", description = "Court management endpoints. Creation and updates are ADMIN-only.")
public class CourtController {

    private final CourtService courtService;

    public CourtController(CourtService courtService) {
        this.courtService = courtService;
    }

    @GetMapping("/hello")
    @Operation(summary = "Hello World", description = "Simple hello world endpoint for service availability check")
    @ApiResponse(responseCode = "200", description = "Hello message returned")
    public ResponseEntity<java.util.Map<String, String>> hello() {
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Hello World from Court Service");
        response.put("service", "court-service");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sports")
    @Operation(summary = "Get sport types", description = "Returns a list of all available sport types defined in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of sport types returned")
    })
    public ResponseEntity<List<SportType>> getSportTypes() {
        log.info("Get sport types request received");
        return ResponseEntity.ok(Arrays.asList(SportType.values()));
    }
    @PostMapping
    @Operation(summary = "Create court", description = "Creates a new court. Default status is ACTIVE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Court created"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<CourtResponseDto> createCourt(@Valid @RequestBody CourtRequestDto request) {
        log.info("Create court request received: name={}, sportType={}, surfaceType={}",
                request.getName(), request.getSportType(), request.getSurfaceType());
        CourtResponseDto court = courtService.createCourt(request);
        log.info("Court created successfully: courtId={}", court.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(court);
    }

    @GetMapping
    @Operation(summary = "List courts", description = "Returns courts filtered optionally by sportType, surfaceType, and status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Courts returned")
    })
    public ResponseEntity<List<CourtResponseDto>> listCourts(
            @RequestParam(required = false) SportType sportType,
            @RequestParam(required = false) SurfaceType surfaceType,
            @RequestParam(required = false) CourtStatus status) {
        log.info("List courts request received: sportType={}, surfaceType={}, status={}", sportType, surfaceType, status);
        List<CourtResponseDto> courts = courtService.listCourts(sportType, surfaceType, status);
        log.info("Courts listed successfully: count={}", courts.size());
        return ResponseEntity.ok(courts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get court", description = "Returns court details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Court found"),
            @ApiResponse(responseCode = "404", description = "Court not found")
    })
    public ResponseEntity<CourtResponseDto> getCourt(@PathVariable UUID id) {
        log.info("Get court request received: courtId={}", id);
        CourtResponseDto court = courtService.getCourt(id);
        log.info("Court returned successfully: courtId={}", id);
        return ResponseEntity.ok(court);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update court status", description = "Updates the status of a court")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "404", description = "Court not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<CourtResponseDto> updateStatus(@PathVariable UUID id,
                                                         @Valid @RequestBody CourtStatusUpdateDto request) {
        log.info("Update court status request received: courtId={}, status={}", id, request.getStatus());
        CourtResponseDto court = courtService.updateStatus(id, request);
        log.info("Court status updated successfully: courtId={}, status={}", id, court.getStatus());
        return ResponseEntity.ok(court);
    }

    @PostMapping("/{courtId}/schedule")
    @Operation(summary = "Create or update schedule", description = "Creates or updates the schedule for a specific day.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schedule saved"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "404", description = "Court not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<CourtScheduleResponseDto> upsertSchedule(@PathVariable UUID courtId,
                                                                    @Valid @RequestBody CourtScheduleRequestDto request) {
        log.info("Upsert court schedule request received: courtId={}, dayOfWeek={}", courtId, request.getDayOfWeek());
        CourtScheduleResponseDto schedule = courtService.upsertSchedule(courtId, request);
        log.info("Court schedule saved successfully: courtId={}, scheduleId={}", courtId, schedule.getId());
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/internal/courts/issues/all")
    @Operation(summary = "Get all court issues (internal)", description = "Retrieve all court issues for analytics purposes - Internal endpoint")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All court issues returned")
    })
    public ResponseEntity<List<com.courthub.common.dto.analytics.CourtIssueInternalDTO>> getAllCourtIssues() {
        log.info("Get all court issues (internal) request received");
        List<com.courthub.common.dto.analytics.CourtIssueInternalDTO> issues = courtService.getAllCourtIssuesForAnalytics();
        log.info("All court issues returned (internal): count={}", issues.size());
        return ResponseEntity.ok(issues);
    }
}
