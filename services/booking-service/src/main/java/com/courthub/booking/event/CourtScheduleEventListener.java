package com.courthub.booking.event;

import com.courthub.booking.repository.TimeSlotRepository;
import com.courthub.booking.domain.TimeSlot;
import com.courthub.booking.domain.TimeSlotStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class CourtScheduleEventListener {

    private final TimeSlotRepository timeSlotRepository;

    @Value("${booking.slot-duration-minutes:60}")
    private int slotDurationMinutes;

    @Value("${booking.slot-generation-days-forward:7}")
    private int daysForward;

    public CourtScheduleEventListener(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    @KafkaListener(topics = "court.schedule.updated",
                   groupId = "booking-service-schedule-events",
                   containerFactory = "courtScheduleKafkaListenerContainerFactory")
    @Transactional
    public void onScheduleUpdated(CourtScheduleEventPayload event) {
        // Generate time slots based on schedule
        LocalTime startTime = event.getOpenTime();
        LocalTime endTime = event.getCloseTime();

        // Generate slots for next N days matching this day of week
        LocalDate today = LocalDate.now();
        for (int i = 0; i < daysForward; i++) {
            LocalDate slotDate = today.plusDays(i);
            if (slotDate.getDayOfWeek().getValue() == event.getDayOfWeek()) {
                generateSlotsForDate(event.getCourtId(), slotDate, startTime, endTime);
            }
        }
    }

    private void generateSlotsForDate(java.util.UUID courtId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        LocalTime current = startTime;
        while (current.isBefore(endTime)) {
            LocalTime slotEnd = current.plusMinutes(slotDurationMinutes);
            if (slotEnd.isAfter(endTime)) {
                break;
            }

            // Check if slot already exists before creating
            if (!timeSlotRepository.existsByCourtIdAndDateAndStartTime(courtId, date, current)) {
                TimeSlot slot = new TimeSlot();
                slot.setCourtId(courtId);
                slot.setDate(date);
                slot.setStartTime(current);
                slot.setEndTime(slotEnd);
                slot.setStatus(TimeSlotStatus.AVAILABLE);
                timeSlotRepository.save(slot);
            }

            current = slotEnd;
        }
    }
}
