package com.courthub.payment.event;

import com.courthub.payment.dto.BookingEventPayload;
import com.courthub.payment.service.PaymentService;
import com.stripe.exception.StripeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingEventListener {

    private static final Logger logger = LoggerFactory.getLogger(BookingEventListener.class);

    private final PaymentService paymentService;

    public BookingEventListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "booking.created", groupId = "payment-service")
    public void handleBookingCreated(BookingEventPayload event) {
        logger.info("Received booking.created event for booking: {}", event.bookingId());

        try {
            if ("PENDING_PAYMENT".equals(event.status())) {
                paymentService.createCheckoutSession(event.bookingId(), event.userId());
                logger.info("Checkout session created for booking: {}", event.bookingId());
            }
        } catch (StripeException e) {
            logger.error("Error creating checkout session for booking: {}", event.bookingId(), e);
        } catch (Exception e) {
            logger.error("Unexpected error processing booking.created event", e);
        }
    }
}
