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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/courts")
@Tag(name = "Courts", description = "Court management endpoints. Creation and updates are ADMIN-only.")
public class CourtController {

    private final CourtService courtService;

    public CourtController(CourtService courtService) {
        this.courtService = courtService;
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
        CourtResponseDto court = courtService.createCourt(request);
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
        List<CourtResponseDto> courts = courtService.listCourts(sportType, surfaceType, status);
        return ResponseEntity.ok(courts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get court", description = "Returns court details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Court found"),
            @ApiResponse(responseCode = "404", description = "Court not found")
    })
    public ResponseEntity<CourtResponseDto> getCourt(@PathVariable UUID id) {
        CourtResponseDto court = courtService.getCourt(id);
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
        CourtResponseDto court = courtService.updateStatus(id, request);
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
        CourtScheduleResponseDto schedule = courtService.upsertSchedule(courtId, request);
        return ResponseEntity.ok(schedule);
    }
}
