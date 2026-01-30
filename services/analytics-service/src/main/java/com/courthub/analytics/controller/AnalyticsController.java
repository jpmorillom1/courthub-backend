package com.courthub.analytics.controller;

import com.courthub.analytics.dto.DashboardResponse;
import com.courthub.analytics.service.AnalyticsService;
import com.courthub.analytics.service.DataSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/analytics")
@Tag(name = "Analytics", description = "Analytics and data synchronization endpoints. Most operations are ADMIN-only.")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final DataSyncService dataSyncService;

    public AnalyticsController(AnalyticsService analyticsService, DataSyncService dataSyncService) {
        this.analyticsService = analyticsService;
        this.dataSyncService = dataSyncService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get analytics dashboard", description = "Returns dashboard metrics including KPIs, heatmap, faculty usage, and student rankings. Requires ADMIN authority.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard data returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<DashboardResponse> getDashboard() {
        log.info("Fetching analytics dashboard [ADMIN]");
        DashboardResponse dashboard = analyticsService.getDashboard();
        return ResponseEntity.ok(dashboard);
    }

    @PostMapping("/sync")
    @Operation(summary = "Trigger manual synchronization", description = "Triggers a manual sync process for analytics data. Requires ADMIN authority.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Synchronization completed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
            @ApiResponse(responseCode = "500", description = "Internal server error during sync")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Map<String, Object>> manualSync() {
        log.info("Manual sync request received [ADMIN]");
        try {
            long startTime = System.currentTimeMillis();
            dataSyncService.syncAnalyticsData();
            long duration = System.currentTimeMillis() - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Analytics data synchronized successfully");
            response.put("timestamp", System.currentTimeMillis());
            response.put("duration_ms", duration);

            log.info("Manual sync completed in {} ms [ADMIN]", duration);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during manual sync", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to synchronize analytics data: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}