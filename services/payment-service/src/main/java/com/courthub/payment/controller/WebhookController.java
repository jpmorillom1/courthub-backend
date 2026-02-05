package com.courthub.payment.controller;

import com.courthub.payment.service.PaymentService;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhook")
public class WebhookController {


    private final PaymentService paymentService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    public WebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Invalid signature", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        log.info("Received Stripe webhook event: {}", event.getType());

        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;
            case "checkout.session.expired":
                handleCheckoutSessionExpired(event);
                break;
            default:
                log.info("Unhandled event type: {}", event.getType());
        }

        return ResponseEntity.ok("Success");
    }

    private void handleCheckoutSessionCompleted(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();

        Session session = (Session) dataObjectDeserializer.getObject()
                .orElseGet(() -> {
                    try {
                        return (Session) dataObjectDeserializer.deserializeUnsafe();
                    } catch (EventDataObjectDeserializationException e) {
                        log.error("Deserialization error", e);
                        throw new RuntimeException(e);
                    }
                });

        if (session != null) {
            try {
                paymentService.handleSuccessfulPayment(session.getId());
            } catch (StripeException e) {
                log.error("Error processing pay: {}", e.getMessage());
            }
        }
    }

    private void handleCheckoutSessionExpired(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();

        Session session = (Session) dataObjectDeserializer.getObject()
                .orElseGet(() -> {
                    try {
                        return (Session) dataObjectDeserializer.deserializeUnsafe();
                    } catch (EventDataObjectDeserializationException e) {
                        log.error("Deserialization error", e);
                        throw new RuntimeException(e);
                    }
                });

        if (session != null) {
            paymentService.handleExpiredPayment(session.getId());
            log.info("Successfully processed checkout.session.expired for session: {}", session.getId());
        }
    }
}