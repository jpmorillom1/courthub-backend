package com.courthub.booking.service;

import com.courthub.booking.domain.Booking;
import com.courthub.booking.domain.BookingStatus;
import com.courthub.booking.domain.ConflictException;
import com.courthub.booking.domain.TimeSlot;
import com.courthub.booking.domain.TimeSlotStatus;
import com.courthub.booking.dto.AvailabilitySlotResponse;
import com.courthub.booking.dto.BookingResponse;
import com.courthub.booking.dto.CreateBookingRequest;
import com.courthub.booking.event.BookingEventProducer;
import com.courthub.booking.repository.BookingRepository;
import com.courthub.booking.repository.TimeSlotRepository;
import com.courthub.common.exception.BusinessException;
import com.courthub.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        booking.setStatus(BookingStatus.CONFIRMED);

        Booking saved = bookingRepository.save(booking);

        timeSlot.setStatus(TimeSlotStatus.BOOKED);
        timeSlotRepository.save(timeSlot);

        bookingEventProducer.sendBookingCreated(saved, timeSlot);

        return toBookingResponse(saved, timeSlot);
    }

    public List<AvailabilitySlotResponse> getAvailableSlots(UUID courtId, LocalDate date) {
        return timeSlotRepository.findByCourtIdAndDateAndStatusOrderByStartTime(
                        courtId,
                        date,
                        TimeSlotStatus.AVAILABLE
                ).stream()
                .map(this::toAvailabilityResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingResponse cancelBooking(UUID bookingId) {
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

        return toBookingResponse(saved, timeSlot);
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
}