package com.courthub.payment.controller;

import com.courthub.common.exception.UnauthorizedException;
import com.courthub.payment.config.JwtAuthenticationToken;
import com.courthub.payment.dto.PaymentResponse;
import com.courthub.payment.service.PaymentService;
import com.stripe.exception.StripeException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
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
        PaymentResponse response = paymentService.createCheckoutSession(bookingId, userId);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/user")
    public ResponseEntity<List<PaymentResponse>> getUserPayments(
            @AuthenticationPrincipal JwtAuthenticationToken auth
    ) {
        List<PaymentResponse> payments = paymentService.getUserPayments(auth.getUserId());
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentResponse> getPaymentByBooking(@PathVariable UUID bookingId) {
        PaymentResponse payment = paymentService.getPaymentByBookingId(bookingId);
        return ResponseEntity.ok(payment);
    }
}
