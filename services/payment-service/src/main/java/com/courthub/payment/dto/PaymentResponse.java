package com.courthub.payment.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID bookingId,
        UUID userId,
        Long amount,
        String currency,
        String status,
        String checkoutUrl,
        LocalDateTime createdAt
) {
}
