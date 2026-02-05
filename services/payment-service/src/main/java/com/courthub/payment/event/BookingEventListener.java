package com.courthub.payment.event;

import com.courthub.payment.dto.BookingEventPayload;
import com.courthub.payment.service.PaymentService;
import com.stripe.exception.StripeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BookingEventListener {

    private final PaymentService paymentService;

    public BookingEventListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "booking.created", groupId = "payment-service")
    public void handleBookingCreated(BookingEventPayload event) {
        log.info("Received booking.created event: bookingId={}", event.bookingId());

        try {
            if ("PENDING_PAYMENT".equals(event.status())) {
                paymentService.createCheckoutSession(event.bookingId(), event.userId());
                log.info("Checkout session created for bookingId={}", event.bookingId());
            } else {
                log.debug("Skipping checkout creation: bookingId={}, status={}", event.bookingId(), event.status());
            }
        } catch (StripeException e) {
            log.error("Error creating checkout session: bookingId={}", event.bookingId(), e);
        } catch (Exception e) {
            log.error("Unexpected error processing booking.created event", e);
        }
    }
}
