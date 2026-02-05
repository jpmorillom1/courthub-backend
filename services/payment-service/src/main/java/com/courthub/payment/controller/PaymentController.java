package com.courthub.payment.controller;

import com.courthub.common.exception.UnauthorizedException;
import com.courthub.payment.config.JwtAuthenticationToken;
import com.courthub.payment.dto.PaymentResponse;
import com.courthub.payment.service.PaymentService;
import com.stripe.exception.StripeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/hello")
    public ResponseEntity<java.util.Map<String, String>> hello() {
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Hello World from Payment Service");
        response.put("service", "payment-service");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/checkout")
    public ResponseEntity<PaymentResponse> createCheckout(
            @RequestBody Map<String, String> request,
            Authentication authentication
    ) throws StripeException {

        if (authentication == null) {
            throw new UnauthorizedException("authentication info not found");
        }


        JwtAuthenticationToken auth = (JwtAuthenticationToken) authentication;
        UUID userId = auth.getUserId();

        UUID bookingId = UUID.fromString(request.get("bookingId"));
        log.info("Create checkout request received: userId={}, bookingId={}", userId, bookingId);
        PaymentResponse response = paymentService.createCheckoutSession(bookingId, userId);

        log.info("Checkout session created successfully: bookingId={}", bookingId);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/user")
    public ResponseEntity<List<PaymentResponse>> getUserPayments(
            @AuthenticationPrincipal JwtAuthenticationToken auth
    ) {
        log.info("Get user payments request received: userId={}", auth.getUserId());
        List<PaymentResponse> payments = paymentService.getUserPayments(auth.getUserId());
        log.info("User payments returned: userId={}, count={}", auth.getUserId(), payments.size());
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentResponse> getPaymentByBooking(@PathVariable UUID bookingId) {
        log.info("Get payment by booking request received: bookingId={}", bookingId);
        PaymentResponse payment = paymentService.getPaymentByBookingId(bookingId);
        log.info("Payment returned successfully: bookingId={}", bookingId);
        return ResponseEntity.ok(payment);
    }
}
