package com.courthub.realtime.service;

import com.courthub.realtime.client.BookingServiceFeignClient;
import com.courthub.realtime.dto.AvailabilitySlotResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {

    private final BookingServiceFeignClient bookingServiceFeignClient;
    private final FirebaseService firebaseService;

    /**
     * Reconciliation loop that ensures eventual consistency between PostgreSQL and Firebase.
     * Runs every 15 minutes.
     * 
     * Logic:
     * 1. Iterate through the current date and the next 7 days.
     * 2. For each date, call the booking-service internal endpoint.
     * 3. For each slot received, update Firebase with the current status.
     * 4. Log connection errors but continue execution.
     */
    @Scheduled(cron = "0 */15 * * * *")
    public void reconcileAvailability() {
        long start = System.currentTimeMillis();
        log.info("Starting reconciliation loop");
        
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(7);
        
        try {
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                reconcileDateSlots(date);
            }
            long durationMs = System.currentTimeMillis() - start;
            log.info("Reconciliation loop completed successfully: durationMs={}", durationMs);
        } catch (Exception e) {
            log.error("Unexpected error during reconciliation loop", e);
        }
    }

    private void reconcileDateSlots(LocalDate date) {
        try {
            log.debug("Reconciling slots for date: {}", date);
            
            // Fetch all slots for the date from booking service
            List<AvailabilitySlotResponse> slots = getSlotsByDateSafely(date);
            
            if (slots == null || slots.isEmpty()) {
                log.debug("No slots found for date: {}", date);
                return;
            }
            
            // Update Firebase with current status for each slot
            for (AvailabilitySlotResponse slot : slots) {
                try {
                    String status = slot.getStatus().toString();
                    firebaseService.updateAvailability(
                            slot.getCourtId(),
                            slot.getDate(),
                            slot.getStartTime(),
                            status
                    );
                } catch (Exception e) {
                    log.error("Failed to update Firebase for slot {} on date {}", slot.getId(), date, e);
                    // Continue with next slot despite error
                }
            }
            
            log.debug("Successfully reconciled {} slots for date {}", slots.size(), date);
        } catch (Exception e) {
            log.error("Failed to reconcile slots for date {}", date, e);
            // Continue with next date despite error
        }
    }

    private List<AvailabilitySlotResponse> getSlotsByDateSafely(LocalDate date) {
        try {
            List<AvailabilitySlotResponse> slots = bookingServiceFeignClient.getSlotsByDate(date);
            log.info("Successfully retrieved {} slots for date {}", slots != null ? slots.size() : 0, date);
            return slots != null ? slots : Collections.emptyList();
        } catch (FeignException e) {
            log.warn("Failed to fetch slots from booking service for date {}", date, e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Unexpected error while fetching slots from booking service for date {}", date, e);
            return Collections.emptyList();
        }
    }
}
