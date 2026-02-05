package com.courthub.booking.event;

import com.courthub.booking.domain.CourtSnapshot;
import com.courthub.booking.repository.CourtSnapshotRepository;
import com.courthub.booking.repository.TimeSlotRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
public class CourtEventListener {

    private final CourtSnapshotRepository courtSnapshotRepository;
    private final TimeSlotRepository timeSlotRepository;

    public CourtEventListener(CourtSnapshotRepository courtSnapshotRepository, TimeSlotRepository timeSlotRepository) {
        this.courtSnapshotRepository = courtSnapshotRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    @KafkaListener(topics = {"court.created", "court.updated", "court.status.changed"},
                   groupId = "booking-service-court-events",
                   containerFactory = "courtEventKafkaListenerContainerFactory")
    @Transactional
    public void onCourtEvent(CourtEventPayload event) {
        log.info("Received court event: courtId={}, status={}", event.getCourtId(), event.getStatus());
        CourtSnapshot snapshot = new CourtSnapshot();
        snapshot.setCourtId(event.getCourtId());
        snapshot.setStatus(event.getStatus());
        snapshot.setSportType(event.getSportType());
        snapshot.setSurfaceType(event.getSurfaceType());
        snapshot.setCapacity(event.getCapacity());
        snapshot.setUpdatedAt(Instant.now());

        courtSnapshotRepository.save(snapshot);
        log.debug("Court snapshot saved: courtId={}", event.getCourtId());
    }
}
