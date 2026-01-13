package com.courthub.booking.event;

import com.courthub.booking.domain.CourtSnapshot;
import com.courthub.booking.domain.CourtStatus;
import com.courthub.booking.repository.CourtSnapshotRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class CourtEventConsumer {

    private final CourtSnapshotRepository courtSnapshotRepository;

    public CourtEventConsumer(CourtSnapshotRepository courtSnapshotRepository) {
        this.courtSnapshotRepository = courtSnapshotRepository;
    }

    @KafkaListener(topics = {"court.created", "court.updated", "court.status.changed"},
                   groupId = "booking-service-court-events",
                   containerFactory = "courtEventKafkaListenerContainerFactory")
    @Transactional
    public void onCourtEvent(CourtEventPayload event) {
        CourtSnapshot snapshot = new CourtSnapshot();
        snapshot.setCourtId(event.getCourtId());
        snapshot.setStatus(event.getStatus());
        snapshot.setSportType(event.getSportType());
        snapshot.setSurfaceType(event.getSurfaceType());
        snapshot.setCapacity(event.getCapacity());
        snapshot.setUpdatedAt(Instant.now());

        courtSnapshotRepository.save(snapshot);
    }
}
