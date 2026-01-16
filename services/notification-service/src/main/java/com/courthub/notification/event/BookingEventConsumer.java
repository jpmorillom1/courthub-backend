package com.courthub.notification.event;

import com.courthub.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final NotificationService notificationService;


    @KafkaListener(topics = "booking.created", groupId = "notification-service-group")
    public void handleBookingCreated(BookingEventPayload event) {
        log.info("Received booking.created event for booking ID: {}", event.getBookingId());

        try {
            notificationService.sendBookingConfirmation(event);
            log.info("Successfully processed booking.created event for booking ID: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Failed to process booking.created event for booking ID: {}", event.getBookingId(), e);
        }
    }
}
