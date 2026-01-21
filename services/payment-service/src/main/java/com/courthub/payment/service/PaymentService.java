package com.courthub.payment.service;

import com.courthub.payment.domain.Payment;
import com.courthub.payment.domain.PaymentStatus;
import com.courthub.payment.dto.PaymentResponse;
import com.courthub.payment.event.PaymentEventProducer;
import com.courthub.payment.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @Value("${stripe.price-per-hour}")
    private Long pricePerHour;

    public PaymentService(PaymentRepository paymentRepository, PaymentEventProducer paymentEventProducer) {
        this.paymentRepository = paymentRepository;
        this.paymentEventProducer = paymentEventProducer;
    }

    @Transactional
    public PaymentResponse createCheckoutSession(UUID bookingId, UUID userId) throws StripeException {
        if (paymentRepository.findByBookingId(bookingId).isPresent()) {
            throw new IllegalStateException("Payment already exists for booking: " + bookingId);
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(pricePerHour)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Court Booking")
                                                                .setDescription("Court reservation for booking ID: " + bookingId)
                                                                .build()
                                                )
                                                .build()
                                )
                                .setQuantity(1L)
                                .build()
                )
                .putMetadata("bookingId", bookingId.toString())
                .putMetadata("userId", userId.toString())
                .build();

        Session session = Session.create(params);

        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setUserId(userId);
        payment.setAmount(pricePerHour);
        payment.setCurrency("usd");
        payment.setStripeSessionId(session.getId());
        payment.setStatus(PaymentStatus.PENDING);

        Payment saved = paymentRepository.save(payment);
        logger.info("Payment created with session ID: {} for booking: {}", session.getId(), bookingId);

        return new PaymentResponse(
                saved.getId(),
                saved.getBookingId(),
                saved.getUserId(),
                saved.getAmount(),
                saved.getCurrency(),
                saved.getStatus().name(),
                session.getUrl(),
                saved.getCreatedAt()
        );
    }

    @Transactional
    public void handleSuccessfulPayment(String sessionId) throws StripeException {
        Session session = Session.retrieve(sessionId);
        
        Payment payment = paymentRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new IllegalStateException("Payment not found for session: " + sessionId));

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setStripePaymentIntentId(session.getPaymentIntent());
        paymentRepository.save(payment);

        logger.info("Payment completed for booking: {}", payment.getBookingId());

        paymentEventProducer.sendPaymentConfirmed(payment);
    }

    @Transactional
    public void handleFailedPayment(String sessionId) {
        paymentRepository.findByStripeSessionId(sessionId).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            logger.warn("Payment failed for booking: {}", payment.getBookingId());
            
            paymentEventProducer.sendPaymentFailed(payment);
        });
    }

    public List<PaymentResponse> getUserPayments(UUID userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(this::toPaymentResponse)
                .collect(Collectors.toList());
    }

    public PaymentResponse getPaymentByBookingId(UUID bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalStateException("Payment not found for booking: " + bookingId));
        return toPaymentResponse(payment);
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBookingId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().name(),
                null,
                payment.getCreatedAt()
        );
    }
}
