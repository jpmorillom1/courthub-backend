package com.courthub.notification.event;

import com.courthub.common.dto.PaymentEventPayload;
import com.courthub.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "payment.confirmed", groupId = "notification-service-group")
    public void handlePaymentConfirmed(PaymentEventPayload event) {
        log.info("Received payment.confirmed event for booking ID: {}", event.bookingId());

        try {
            notificationService.sendPaymentConfirmationEmail(event);
            log.info("Successfully processed payment.confirmed event for booking ID: {}", event.bookingId());
        } catch (Exception e) {
            log.error("Failed to process payment.confirmed event for booking ID: {}", event.bookingId(), e);
        }
    }
}
