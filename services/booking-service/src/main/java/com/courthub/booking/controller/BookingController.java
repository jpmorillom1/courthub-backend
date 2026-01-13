package com.courthub.booking.controller;

import com.courthub.booking.config.JwtAuthenticationToken;
import com.courthub.booking.dto.AvailabilitySlotResponse;
import com.courthub.booking.dto.BookingResponse;
import com.courthub.booking.dto.CreateBookingRequest;
import com.courthub.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
@Tag(name = "Bookings", description = "Booking management endpoints")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @Operation(summary = "Create booking", description = "Creates a new booking for an available time slot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Booking confirmed"),
            @ApiResponse(responseCode = "404", description = "Time slot not found"),
            @ApiResponse(responseCode = "409", description = "Time slot already booked"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        UUID userId = extractUserId();
        BookingResponse booking = bookingService.createBooking(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @GetMapping("/availability")
    @Operation(summary = "Get available slots", description = "Query available time slots for a court on a specific date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Available slots returned")
    })
    public ResponseEntity<List<AvailabilitySlotResponse>> getAvailableSlots(
            @RequestParam UUID courtId,
            @RequestParam LocalDate date) {
        List<AvailabilitySlotResponse> slots = bookingService.getAvailableSlots(courtId, date);
        return ResponseEntity.ok(slots);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel booking", description = "Cancels an existing booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking cancelled"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "400", description = "Invalid state for cancellation"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable UUID id) {
        BookingResponse booking = bookingService.cancelBooking(id);
        return ResponseEntity.ok(booking);
    }

    private UUID extractUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new com.courthub.common.exception.UnauthorizedException("User not authenticated");
        }
        return ((JwtAuthenticationToken) authentication).getUserId();
    }
}
