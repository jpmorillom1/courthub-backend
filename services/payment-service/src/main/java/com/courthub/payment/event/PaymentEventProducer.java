package com.courthub.payment.event;

import com.courthub.common.dto.PaymentEventPayload;
import com.courthub.payment.domain.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
@Component
public class PaymentEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventProducer.class);

    private static final String PAYMENT_CONFIRMED_TOPIC = "payment.confirmed";
    private static final String PAYMENT_FAILED_TOPIC = "payment.failed";

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

        logger.info("Published payment.confirmed event for booking: {}", payment.getBookingId());
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

        logger.info("Published payment.failed event for booking: {}", payment.getBookingId());
    }
}
