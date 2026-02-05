package com.courthub.common.dto;

import java.util.UUID;

public record PaymentEventPayload(
        UUID paymentId,
        UUID bookingId,
        UUID userId,
        Long amount,
        String currency,
        String status
) {
}
