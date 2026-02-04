package com.courthub.booking.controller;

import com.courthub.booking.config.JwtAuthenticationToken;
import com.courthub.common.dto.AvailabilitySlotResponse;
import com.courthub.booking.dto.BookingResponse;
import com.courthub.booking.dto.CreateBookingRequest;
import com.courthub.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/bookings")
@Tag(name = "Bookings", description = "Booking management endpoints")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/hello")
    @Operation(summary = "Hello World", description = "Simple hello world endpoint for service availability check")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hello message returned")
    })
    public ResponseEntity<java.util.Map<String, String>> hello() {
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Hello World from Booking Service");
        response.put("service", "booking-service");
        return ResponseEntity.ok(response);
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
        log.info("Create booking request received: userId={}, courtId={}, date={}", userId, request.getCourtId(), request.getDate());
        BookingResponse booking = bookingService.createBooking(userId, request);
        log.info("Booking created successfully: bookingId={}, userId={}", booking.getId(), userId);
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
        log.info("Get available slots request received: courtId={}, date={}", courtId, date);
        List<AvailabilitySlotResponse> slots = bookingService.getAvailableSlots(courtId, date);
        log.info("Available slots returned: courtId={}, date={}, count={}", courtId, date, slots.size());
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/internal/slots-sync")
    @Operation(summary = "Get all slots by date (internal)", description = "Retrieve all time slots (Available, Booked, or Blocked) for a given date - Internal endpoint for realtime synchronization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All slots returned")
    })
    public ResponseEntity<List<AvailabilitySlotResponse>> getAllSlotsByDate(
            @RequestParam LocalDate date) {
        log.info("Get all slots by date request received: date={}", date);
        List<AvailabilitySlotResponse> slots = bookingService.getAllSlotsByDate(date);
        log.info("All slots returned: date={}, count={}", date, slots.size());
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/internal/bookings/all")
    @Operation(summary = "Get all bookings (internal)", description = "Retrieve all bookings for analytics purposes - Internal endpoint")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All bookings returned")
    })
    public ResponseEntity<List<com.courthub.common.dto.analytics.BookingInternalDTO>> getAllBookings() {
        log.info("Get all bookings (internal) request received");
        List<com.courthub.common.dto.analytics.BookingInternalDTO> bookings = bookingService.getAllBookingsForAnalytics();
        log.info("All bookings returned (internal): count={}", bookings.size());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user bookings", description = "Retrieves all bookings for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User bookings returned"),
            @ApiResponse(responseCode = "404", description = "No bookings found")
    })
    public ResponseEntity<List<BookingResponse>> getUserBookings(@PathVariable UUID userId) {
        log.info("Get user bookings request received: userId={}", userId);
        List<BookingResponse> bookings = bookingService.getBookingsByUserId(userId);
        log.info("User bookings returned: userId={}, count={}", userId, bookings.size());
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking details", description = "Retrieves details of a specific booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking details returned"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable UUID id) {
        log.info("Get booking by id request received: bookingId={}", id);
        BookingResponse booking = bookingService.getBookingById(id);
        log.info("Booking returned successfully: bookingId={}", id);
        return ResponseEntity.ok(booking);
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
        log.info("Cancel booking request received: bookingId={}", id);
        BookingResponse booking = bookingService.cancelBooking(id);
        log.info("Booking cancelled successfully: bookingId={}", id);
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
