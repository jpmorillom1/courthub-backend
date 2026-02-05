package com.courthub.payment.event;

import com.courthub.common.dto.PaymentEventPayload;
import com.courthub.payment.domain.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
@Component
@Slf4j
public class PaymentEventProducer {

    private static final String PAYMENT_CONFIRMED_TOPIC = "payment.confirmed";
    private static final String PAYMENT_FAILED_TOPIC = "payment.failed";
    private static final String PAYMENT_EXPIRED_TOPIC = "payment.expired";

    private final KafkaTemplate<String, PaymentEventPayload> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, PaymentEventPayload> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentConfirmed(Payment payment) {
        PaymentEventPayload payload = new PaymentEventPayload(
                payment.getId(),
                payment.getBookingId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().name()
        );

        kafkaTemplate.send(
                PAYMENT_CONFIRMED_TOPIC,
                payment.getBookingId().toString(),
                payload
        );

        log.info("Published payment.confirmed event: bookingId={}", payment.getBookingId());
    }

    public void sendPaymentFailed(Payment payment) {
        PaymentEventPayload payload = new PaymentEventPayload(
                payment.getId(),
                payment.getBookingId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().name()
        );

        kafkaTemplate.send(
                PAYMENT_FAILED_TOPIC,
                payment.getBookingId().toString(),
                payload
        );

        log.info("Published payment.failed event: bookingId={}", payment.getBookingId());
    }

    public void sendPaymentExpired(Payment payment) {
        PaymentEventPayload payload = new PaymentEventPayload(
                payment.getId(),
                payment.getBookingId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().name()
        );

        kafkaTemplate.send(
                PAYMENT_EXPIRED_TOPIC,
                payment.getBookingId().toString(),
                payload
        );

        log.info("Published payment.expired event: bookingId={}", payment.getBookingId());
    }
}
