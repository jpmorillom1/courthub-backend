package com.courthub.booking.service;

import com.courthub.booking.domain.Booking;
import com.courthub.booking.domain.BookingStatus;
import com.courthub.booking.domain.ConflictException;
import com.courthub.booking.domain.TimeSlot;
import com.courthub.booking.dto.BookingResponse;
import com.courthub.booking.dto.CreateBookingRequest;
import com.courthub.booking.event.BookingEventProducer;
import com.courthub.booking.repository.BookingRepository;
import com.courthub.booking.repository.TimeSlotRepository;
import com.courthub.common.dto.AvailabilitySlotResponse;
import com.courthub.common.dto.enums.TimeSlotStatus;
import com.courthub.common.exception.BusinessException;
import com.courthub.common.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Unit Tests")
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private BookingEventProducer bookingEventProducer;

    @InjectMocks
    private BookingService bookingService;

    private UUID userId;
    private UUID courtId;
    private UUID bookingId;
    private UUID timeSlotId;
    private LocalDate testDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private TimeSlot testTimeSlot;
    private Booking testBooking;
    private CreateBookingRequest createBookingRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        courtId = UUID.randomUUID();
        bookingId = UUID.randomUUID();
        timeSlotId = UUID.randomUUID();
        testDate = LocalDate.now().plusDays(1);
        startTime = LocalTime.of(9, 0);
        endTime = LocalTime.of(10, 0);

        testTimeSlot = new TimeSlot();
        testTimeSlot.setId(timeSlotId);
        testTimeSlot.setCourtId(courtId);
        testTimeSlot.setDate(testDate);
        testTimeSlot.setStartTime(startTime);
        testTimeSlot.setEndTime(endTime);
        testTimeSlot.setStatus(TimeSlotStatus.AVAILABLE);

        testBooking = new Booking();
        testBooking.setId(bookingId);
        testBooking.setTimeSlotId(timeSlotId);
        testBooking.setCourtId(courtId);
        testBooking.setUserId(userId);
        testBooking.setStatus(BookingStatus.PENDING_PAYMENT);

        createBookingRequest = new CreateBookingRequest();
        createBookingRequest.setCourtId(courtId);
        createBookingRequest.setDate(testDate);
        createBookingRequest.setStartTime(startTime);
    }

    @Test
    @DisplayName("Should create booking successfully")
    void testCreateBookingSuccess() {
        // Arrange
        when(timeSlotRepository.findByCourtIdAndDateAndStartTime(courtId, testDate, startTime))
                .thenReturn(Optional.of(testTimeSlot));
        when(bookingRepository.existsByTimeSlotIdAndStatus(timeSlotId, BookingStatus.CONFIRMED))
                .thenReturn(false);
        
        Booking savedBooking = new Booking();
        savedBooking.setId(bookingId);
        savedBooking.setTimeSlotId(timeSlotId);
        savedBooking.setCourtId(courtId);
        savedBooking.setUserId(userId);
        savedBooking.setStatus(BookingStatus.PENDING_PAYMENT);
        
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(testTimeSlot);
        doNothing().when(bookingEventProducer).sendBookingCreated(any(Booking.class), any(TimeSlot.class));

        // Act
        BookingResponse result = bookingService.createBooking(userId, createBookingRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(bookingId);
        assertThat(result.getCourtId()).isEqualTo(courtId);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getDate()).isEqualTo(testDate);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);

        verify(timeSlotRepository, times(1)).findByCourtIdAndDateAndStartTime(courtId, testDate, startTime);
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(bookingEventProducer, times(1)).sendBookingCreated(any(Booking.class), any(TimeSlot.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when creating booking for past date")
    void testCreateBookingWithPastDate() {
        // Arrange
        createBookingRequest.setDate(LocalDate.now().minusDays(1));

        // Act & Assert
        assertThatThrownBy(() -> bookingService.createBooking(userId, createBookingRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot create booking for past dates");

        verify(timeSlotRepository, never()).findByCourtIdAndDateAndStartTime(any(), any(), any());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when time slot not found")
    void testCreateBookingTimeSlotNotFound() {
        // Arrange
        when(timeSlotRepository.findByCourtIdAndDateAndStartTime(courtId, testDate, startTime))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookingService.createBooking(userId, createBookingRequest))
                .isInstanceOf(NotFoundException.class);

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw ConflictException when time slot is not available")
    void testCreateBookingTimeSlotNotAvailable() {
        // Arrange
        testTimeSlot.setStatus(TimeSlotStatus.BOOKED);
        when(timeSlotRepository.findByCourtIdAndDateAndStartTime(courtId, testDate, startTime))
                .thenReturn(Optional.of(testTimeSlot));

        // Act & Assert
        assertThatThrownBy(() -> bookingService.createBooking(userId, createBookingRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Time slot is not available");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw ConflictException when time slot is already booked")
    void testCreateBookingTimeSlotAlreadyBooked() {
        // Arrange
        when(timeSlotRepository.findByCourtIdAndDateAndStartTime(courtId, testDate, startTime))
                .thenReturn(Optional.of(testTimeSlot));
        when(bookingRepository.existsByTimeSlotIdAndStatus(timeSlotId, BookingStatus.CONFIRMED))
                .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> bookingService.createBooking(userId, createBookingRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Time slot is already booked");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should get available slots successfully")
    void testGetAvailableSlotsSuccess() {
        // Arrange
        List<TimeSlot> timeSlots = List.of(testTimeSlot);
        when(timeSlotRepository.findByCourtIdAndDateAndStatusOrderByStartTime(
                courtId, testDate, TimeSlotStatus.AVAILABLE))
                .thenReturn(timeSlots);

        // Act
        List<AvailabilitySlotResponse> result = bookingService.getAvailableSlots(courtId, testDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCourtId()).isEqualTo(courtId);
        assertThat(result.get(0).getDate()).isEqualTo(testDate);

        verify(timeSlotRepository, times(1))
                .findByCourtIdAndDateAndStatusOrderByStartTime(courtId, testDate, TimeSlotStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Should return empty list when no available slots found")
    void testGetAvailableSlotsEmpty() {
        // Arrange
        when(timeSlotRepository.findByCourtIdAndDateAndStatusOrderByStartTime(
                courtId, testDate, TimeSlotStatus.AVAILABLE))
                .thenReturn(Collections.emptyList());

        // Act
        List<AvailabilitySlotResponse> result = bookingService.getAvailableSlots(courtId, testDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should get all slots by date successfully")
    void testGetAllSlotsByDateSuccess() {
        // Arrange
        List<TimeSlot> timeSlots = List.of(testTimeSlot);
        when(timeSlotRepository.findByDate(testDate)).thenReturn(timeSlots);

        // Act
        List<AvailabilitySlotResponse> result = bookingService.getAllSlotsByDate(testDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDate()).isEqualTo(testDate);

        verify(timeSlotRepository, times(1)).findByDate(testDate);
    }

    @Test
    @DisplayName("Should return empty list when no slots found for date")
    void testGetAllSlotsByDateEmpty() {
        // Arrange
        when(timeSlotRepository.findByDate(testDate)).thenReturn(Collections.emptyList());

        // Act
        List<AvailabilitySlotResponse> result = bookingService.getAllSlotsByDate(testDate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should get bookings by user ID successfully")
    void testGetBookingsByUserIdSuccess() {
        // Arrange
        List<Booking> bookings = List.of(testBooking);
        when(bookingRepository.findByUserId(userId)).thenReturn(bookings);
        when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(testTimeSlot));

        // Act
        List<BookingResponse> result = bookingService.getBookingsByUserId(userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
        assertThat(result.get(0).getCourtId()).isEqualTo(courtId);

        verify(bookingRepository, times(1)).findByUserId(userId);
        verify(timeSlotRepository, times(1)).findById(timeSlotId);
    }

    @Test
    @DisplayName("Should return empty list when user has no bookings")
    void testGetBookingsByUserIdEmpty() {
        // Arrange
        when(bookingRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // Act
        List<BookingResponse> result = bookingService.getBookingsByUserId(userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(timeSlotRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw NotFoundException when time slot not found for booking")
    void testGetBookingsByUserIdTimeSlotNotFound() {
        // Arrange
        List<Booking> bookings = List.of(testBooking);
        when(bookingRepository.findByUserId(userId)).thenReturn(bookings);
        when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookingService.getBookingsByUserId(userId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Should get booking by ID successfully")
    void testGetBookingByIdSuccess() {
        // Arrange
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));
        when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(testTimeSlot));

        // Act
        BookingResponse result = bookingService.getBookingById(bookingId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(bookingId);
        assertThat(result.getCourtId()).isEqualTo(courtId);

        verify(bookingRepository, times(1)).findById(bookingId);
        verify(timeSlotRepository, times(1)).findById(timeSlotId);
    }

    @Test
    @DisplayName("Should throw NotFoundException when booking not found")
    void testGetBookingByIdNotFound() {
        // Arrange
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookingService.getBookingById(bookingId))
                .isInstanceOf(NotFoundException.class);

        verify(timeSlotRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw NotFoundException when time slot not found during get booking")
    void testGetBookingByIdTimeSlotNotFound() {
        // Arrange
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));
        when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookingService.getBookingById(bookingId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Should cancel booking successfully")
    void testCancelBookingSuccess() {
        // Arrange
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));
        when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.of(testTimeSlot));

        Booking cancelledBooking = new Booking();
        cancelledBooking.setId(bookingId);
        cancelledBooking.setTimeSlotId(timeSlotId);
        cancelledBooking.setCourtId(courtId);
        cancelledBooking.setUserId(userId);
        cancelledBooking.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.save(any(Booking.class))).thenReturn(cancelledBooking);
        when(timeSlotRepository.save(any(TimeSlot.class))).thenReturn(testTimeSlot);
        doNothing().when(bookingEventProducer).sendBookingCancelled(any(Booking.class), any(TimeSlot.class));

        // Act
        BookingResponse result = bookingService.cancelBooking(bookingId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(bookingId);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);

        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(bookingEventProducer, times(1)).sendBookingCancelled(any(Booking.class), any(TimeSlot.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when cancelling non-existent booking")
    void testCancelBookingNotFound() {
        // Arrange
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId))
                .isInstanceOf(NotFoundException.class);

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when cancelling already cancelled booking")
    void testCancelBookingAlreadyCancelled() {
        // Arrange
        testBooking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));

        // Act & Assert
        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Booking is already cancelled");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when time slot not found during cancel")
    void testCancelBookingTimeSlotNotFound() {
        // Arrange
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(testBooking));
        when(timeSlotRepository.findById(timeSlotId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId))
                .isInstanceOf(NotFoundException.class);
    }
}
