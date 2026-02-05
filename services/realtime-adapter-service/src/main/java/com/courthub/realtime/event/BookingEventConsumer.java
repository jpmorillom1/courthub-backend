package com.courthub.realtime.event;

import com.courthub.realtime.dto.BookingEventPayload;
import com.courthub.realtime.service.FirebaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventConsumer {

    private final FirebaseService firebaseService;

    @KafkaListener(topics = "booking.created", groupId = "realtime-adapter-group")
    public void handleBookingCreated(BookingEventPayload payload) {
        log.info("Received booking.created event: bookingId={}, courtId={}, date={}, startTime={}",
                payload.getBookingId(), payload.getCourtId(), payload.getDate(), payload.getStartTime());

        try {
            firebaseService.updateAvailability(
                    payload.getCourtId(),
                    payload.getDate(),
                    payload.getStartTime(),
                    "BOOKED"
            );
            log.info("Successfully synced booking.created to Firebase for booking: {}", payload.getBookingId());
        } catch (Exception e) {
            log.error("Error processing booking.created event for booking: {}", payload.getBookingId(), e);
        }
    }

    @KafkaListener(topics = "booking.cancelled", groupId = "realtime-adapter-group")
    public void handleBookingCancelled(BookingEventPayload payload) {
        log.info("Received booking.cancelled event: bookingId={}, courtId={}, date={}, startTime={}",
                payload.getBookingId(), payload.getCourtId(), payload.getDate(), payload.getStartTime());

        try {
            firebaseService.updateAvailability(
                    payload.getCourtId(),
                    payload.getDate(),
                    payload.getStartTime(),
                    "AVAILABLE"
            );
            log.info("Successfully synced booking.cancelled to Firebase for booking: {}", payload.getBookingId());
        } catch (Exception e) {
            log.error("Error processing booking.cancelled event for booking: {}", payload.getBookingId(), e);
        }
    }
}
