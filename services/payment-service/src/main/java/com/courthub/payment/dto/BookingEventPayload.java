package com.courthub.payment.dto;

import java.util.UUID;

public record BookingEventPayload(
        UUID bookingId,
        UUID userId,
        UUID courtId,
        UUID timeSlotId,
        String status
) {
}
