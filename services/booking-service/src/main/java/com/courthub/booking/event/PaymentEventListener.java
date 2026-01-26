package com.courthub.booking.event;

import com.courthub.booking.domain.Booking;
import com.courthub.booking.domain.BookingStatus;
import com.courthub.booking.repository.BookingRepository;
import com.courthub.booking.service.BookingService;
import com.courthub.common.dto.PaymentEventPayload;
import com.courthub.common.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventListener.class);

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    public PaymentEventListener(BookingRepository bookingRepository, BookingService bookingService) {
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
    }

    @KafkaListener(
            topics = "payment.confirmed",
            groupId = "booking-service",
            containerFactory = "paymentEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentConfirmed(PaymentEventPayload event) {
        logger.info("Received payment.confirmed event for booking: {}", event.bookingId());

        try {
            Booking booking = bookingRepository.findById(event.bookingId())
                    .orElseThrow(() -> new NotFoundException("Booking", event.bookingId()));

            if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);
                logger.info("Booking {} status updated to CONFIRMED", event.bookingId());
            } else {
                logger.warn("Booking {} is not in PENDING_PAYMENT status. Current status: {}", 
                        event.bookingId(), booking.getStatus());
            }
        } catch (Exception e) {
            logger.error("Error processing payment.confirmed event for booking: {}", event.bookingId(), e);
        }
    }

    @KafkaListener(
            topics = "payment.failed",
            groupId = "booking-service",
            containerFactory = "paymentEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentFailed(PaymentEventPayload event) {
        logger.info("Received payment.failed event for booking: {}", event.bookingId());

        try {
            Booking booking = bookingRepository.findById(event.bookingId())
                    .orElseThrow(() -> new NotFoundException("Booking", event.bookingId()));

            if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
                booking.setStatus(BookingStatus.PAYMENT_FAILED);
                bookingRepository.save(booking);
                logger.info("Booking {} status updated to PAYMENT_FAILED", event.bookingId());
            }
        } catch (Exception e) {
            logger.error("Error processing payment.failed event for booking: {}", event.bookingId(), e);
        }
    }

    @KafkaListener(
            topics = "payment.expired",
            groupId = "booking-service",
            containerFactory = "paymentEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentExpired(PaymentEventPayload event) {
        logger.info("Received payment.expired event for booking: {}", event.bookingId());

        try {
            bookingService.handleExpiredPayment(event.bookingId());
            logger.info("Successfully handled expired payment for booking: {}", event.bookingId());
        } catch (Exception e) {
            logger.error("Error processing payment.expired event for booking: {}", event.bookingId(), e);
        }
    }
}
