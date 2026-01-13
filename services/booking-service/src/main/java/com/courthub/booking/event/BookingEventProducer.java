package com.courthub.booking.event;

import com.courthub.booking.domain.Booking;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class BookingEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.booking-created:booking.created}")
    private String bookingCreatedTopic;

    @Value("${kafka.topics.booking-cancelled:booking.cancelled}")
    private String bookingCancelledTopic;

    public BookingEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendBookingCreated(Booking booking) {
        publishAfterCommit(bookingCreatedTopic, mapBookingEvent(booking));
    }

    public void sendBookingCancelled(Booking booking) {
        publishAfterCommit(bookingCancelledTopic, mapBookingEvent(booking));
    }

    private BookingEventPayload mapBookingEvent(Booking booking) {
        return new BookingEventPayload(
                booking.getId(),
                booking.getTimeSlotId(),
                booking.getCourtId(),
                booking.getUserId(),
                booking.getStatus().name()
        );
    }

    private void publishAfterCommit(String topic, Object payload) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    kafkaTemplate.send(topic, payload);
                }
            });
        } else {
            kafkaTemplate.send(topic, payload);
        }
    }
}
