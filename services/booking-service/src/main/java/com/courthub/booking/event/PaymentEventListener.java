package com.courthub.booking.event;

import com.courthub.booking.domain.Booking;
import com.courthub.booking.domain.BookingStatus;
import com.courthub.booking.repository.BookingRepository;
import com.courthub.booking.service.BookingService;
import com.courthub.common.dto.PaymentEventPayload;
import com.courthub.common.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class PaymentEventListener {

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
        log.info("Received payment.confirmed event: bookingId={}", event.bookingId());

        try {
            Booking booking = bookingRepository.findById(event.bookingId())
                    .orElseThrow(() -> new NotFoundException("Booking", event.bookingId()));

            if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
                booking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);
                log.info("Booking status updated to CONFIRMED: bookingId={}", event.bookingId());
            } else {
                log.warn("Booking not in PENDING_PAYMENT status: bookingId={}, status={}", 
                        event.bookingId(), booking.getStatus());
            }
        } catch (Exception e) {
            log.error("Error processing payment.confirmed event: bookingId={}", event.bookingId(), e);
        }
    }

    @KafkaListener(
            topics = "payment.failed",
            groupId = "booking-service",
            containerFactory = "paymentEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentFailed(PaymentEventPayload event) {
        log.info("Received payment.failed event: bookingId={}", event.bookingId());

        try {
            Booking booking = bookingRepository.findById(event.bookingId())
                    .orElseThrow(() -> new NotFoundException("Booking", event.bookingId()));

            if (booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
                booking.setStatus(BookingStatus.PAYMENT_FAILED);
                bookingRepository.save(booking);
                log.info("Booking status updated to PAYMENT_FAILED: bookingId={}", event.bookingId());
            }
        } catch (Exception e) {
            log.error("Error processing payment.failed event: bookingId={}", event.bookingId(), e);
        }
    }

    @KafkaListener(
            topics = "payment.expired",
            groupId = "booking-service",
            containerFactory = "paymentEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentExpired(PaymentEventPayload event) {
        log.info("Received payment.expired event: bookingId={}", event.bookingId());

        try {
            bookingService.handleExpiredPayment(event.bookingId());
            log.info("Expired payment handled successfully: bookingId={}", event.bookingId());
        } catch (Exception e) {
            log.error("Error processing payment.expired event: bookingId={}", event.bookingId(), e);
        }
    }
}
