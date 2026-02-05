package com.courthub.booking.event;

import com.courthub.booking.domain.Booking;
import com.courthub.booking.domain.TimeSlot; 
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
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

    
    public void sendBookingCreated(Booking booking, TimeSlot timeSlot) {
        log.info("Publishing booking.created event: bookingId={}, timeSlotId={}", booking.getId(), timeSlot.getId());
        publishAfterCommit(bookingCreatedTopic, mapBookingEvent(booking, timeSlot));
    }

    
    public void sendBookingCancelled(Booking booking, TimeSlot timeSlot) {
        log.info("Publishing booking.cancelled event: bookingId={}, timeSlotId={}", booking.getId(), timeSlot.getId());
        publishAfterCommit(bookingCancelledTopic, mapBookingEvent(booking, timeSlot));
    }

    private BookingEventPayload mapBookingEvent(Booking booking, TimeSlot timeSlot) {
        return new BookingEventPayload(
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