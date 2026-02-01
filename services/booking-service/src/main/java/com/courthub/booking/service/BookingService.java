package com.courthub.booking.service;

import com.courthub.booking.domain.Booking;
import com.courthub.booking.domain.BookingStatus;
import com.courthub.booking.domain.ConflictException;
import com.courthub.booking.domain.TimeSlot;
import com.courthub.common.dto.enums.TimeSlotStatus;
import com.courthub.common.dto.AvailabilitySlotResponse;
import com.courthub.booking.dto.BookingResponse;
import com.courthub.booking.dto.CreateBookingRequest;
import com.courthub.booking.event.BookingEventProducer;
import com.courthub.booking.repository.BookingRepository;
import com.courthub.booking.repository.TimeSlotRepository;
import com.courthub.common.exception.BusinessException;
import com.courthub.common.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final BookingEventProducer bookingEventProducer;

    public BookingService(BookingRepository bookingRepository,
                          TimeSlotRepository timeSlotRepository,
                          BookingEventProducer bookingEventProducer) {
        this.bookingRepository = bookingRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.bookingEventProducer = bookingEventProducer;
    }

    @Transactional
    public BookingResponse createBooking(UUID userId, CreateBookingRequest request) {
        log.info("Creating booking: userId={}, courtId={}, date={}, startTime={}",
                userId, request.getCourtId(), request.getDate(), request.getStartTime());
        if (request.getDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Cannot create booking for past dates");
        }

        TimeSlot timeSlot = timeSlotRepository.findByCourtIdAndDateAndStartTime(
                request.getCourtId(),
                request.getDate(),
                request.getStartTime()
        ).orElseThrow(() -> new NotFoundException("TimeSlot", request.getCourtId() + " on " + request.getDate()));

        if (timeSlot.getStatus() != TimeSlotStatus.AVAILABLE) {
            throw new ConflictException("Time slot is not available");
        }

        if (bookingRepository.existsByTimeSlotIdAndStatus(timeSlot.getId(), BookingStatus.CONFIRMED)) {
            throw new ConflictException("Time slot is already booked");
        }

        Booking booking = new Booking();
        booking.setTimeSlotId(timeSlot.getId());
        booking.setCourtId(request.getCourtId());
        booking.setUserId(userId);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);

        Booking saved = bookingRepository.save(booking);

        timeSlot.setStatus(TimeSlotStatus.BOOKED);
        timeSlotRepository.save(timeSlot);

        bookingEventProducer.sendBookingCreated(saved, timeSlot);

        log.info("Booking created successfully: bookingId={}, timeSlotId={}", saved.getId(), timeSlot.getId());

        return toBookingResponse(saved, timeSlot);
    }

    public List<AvailabilitySlotResponse> getAvailableSlots(UUID courtId, LocalDate date) {
        log.debug("Fetching available slots: courtId={}, date={}", courtId, date);
        return timeSlotRepository.findByCourtIdAndDateAndStatusOrderByStartTime(
                        courtId,
                        date,
                        TimeSlotStatus.AVAILABLE
                ).stream()
                .map(this::toAvailabilityResponse)
                .collect(Collectors.toList());
    }

    public List<AvailabilitySlotResponse> getAllSlotsByDate(LocalDate date) {
        log.debug("Fetching all slots by date: date={}", date);
        return timeSlotRepository.findByDate(date)
                .stream()
                .map(this::toAvailabilityResponse)
                .collect(Collectors.toList());
    }

    public List<BookingResponse> getBookingsByUserId(UUID userId) {
        log.debug("Fetching bookings by userId={}", userId);
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(booking -> {
                    TimeSlot timeSlot = timeSlotRepository.findById(booking.getTimeSlotId())
                            .orElseThrow(() -> new NotFoundException("TimeSlot", booking.getTimeSlotId()));
                    return toBookingResponse(booking, timeSlot);
                })
                .collect(Collectors.toList());
    }

    public BookingResponse getBookingById(UUID bookingId) {
        log.debug("Fetching booking by id={}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking", bookingId));
        TimeSlot timeSlot = timeSlotRepository.findById(booking.getTimeSlotId())
                .orElseThrow(() -> new NotFoundException("TimeSlot", booking.getTimeSlotId()));
        return toBookingResponse(booking, timeSlot);
    }

    @Transactional
    public BookingResponse cancelBooking(UUID bookingId) {
        log.info("Cancelling booking: bookingId={}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking", bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);

        TimeSlot timeSlot = timeSlotRepository.findById(booking.getTimeSlotId())
                .orElseThrow(() -> new NotFoundException("TimeSlot", booking.getTimeSlotId()));

        timeSlot.setStatus(TimeSlotStatus.AVAILABLE);
        timeSlotRepository.save(timeSlot);

        bookingEventProducer.sendBookingCancelled(saved, timeSlot);

        log.info("Booking cancelled successfully: bookingId={}, timeSlotId={}", bookingId, timeSlot.getId());

        return toBookingResponse(saved, timeSlot);
    }

    @Transactional
    public void handleExpiredPayment(UUID bookingId) {
        log.info("Handling expired payment for bookingId={}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking", bookingId));

        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            
            TimeSlot timeSlot = timeSlotRepository.findById(booking.getTimeSlotId())
                    .orElseThrow(() -> new NotFoundException("TimeSlot", booking.getTimeSlotId()));
            
            timeSlot.setStatus(TimeSlotStatus.AVAILABLE);
            timeSlotRepository.save(timeSlot);
            
            log.info("Booking {} cancelled and TimeSlot {} released due to payment expiration",
                    bookingId, timeSlot.getId());
        } else {
            log.warn("Booking {} is not in PENDING_PAYMENT status. Current status: {}",
                    bookingId, booking.getStatus());
        }
    }

    private BookingResponse toBookingResponse(Booking booking, TimeSlot timeSlot) {
        return new BookingResponse(
                booking.getId(),
                booking.getTimeSlotId(),
                booking.getCourtId(),
                booking.getUserId(),
                timeSlot.getDate(),
                timeSlot.getStartTime(),
                timeSlot.getEndTime(),
                booking.getStatus()
        );
    }

    private AvailabilitySlotResponse toAvailabilityResponse(TimeSlot slot) {
        return new AvailabilitySlotResponse(
                slot.getId(),
                slot.getCourtId(),
                slot.getDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getStatus()
        );
    }

    /**
     * Cron Job 2AM for delete old TimeSlots .
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldTimeSlots() {
        long start = System.currentTimeMillis();
        log.info("Starting cleanup of old time slots");
        try {
            timeSlotRepository.deleteOldUnusedSlots(LocalDate.now(), TimeSlotStatus.AVAILABLE);
            long durationMs = System.currentTimeMillis() - start;
            log.info("Completed cleanup of old time slots: durationMs={}", durationMs);
        } catch (Exception e) {
            log.error("Error during time slot cleanup", e);
            throw e;
        }
    }

    public List<com.courthub.common.dto.analytics.BookingInternalDTO> getAllBookingsForAnalytics() {
        log.debug("Fetching all bookings for analytics");
        return bookingRepository.findAll().stream()
            .map(this::toBookingInternalDTO)
            .collect(Collectors.toList());
    }

    private com.courthub.common.dto.analytics.BookingInternalDTO toBookingInternalDTO(Booking booking) {
        TimeSlot timeSlot = timeSlotRepository.findById(booking.getTimeSlotId())
            .orElseThrow(() -> new NotFoundException("TimeSlot", booking.getTimeSlotId().toString()));
        
        return new com.courthub.common.dto.analytics.BookingInternalDTO(
            booking.getId().toString(),
            booking.getCourtId().toString(),
            timeSlot.getDate(),
            timeSlot.getStartTime(),
            booking.getStatus().toString(),
            booking.getUserId().toString()
        );
    }
}