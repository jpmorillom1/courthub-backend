package com.courthub.court.event;

import com.courthub.court.domain.Court;
import com.courthub.court.domain.CourtSchedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
public class CourtEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.court-created:court.created}")
    private String courtCreatedTopic;

    @Value("${kafka.topics.court-updated:court.updated}")
    private String courtUpdatedTopic;

    @Value("${kafka.topics.court-status-changed:court.status.changed}")
    private String courtStatusChangedTopic;

    @Value("${kafka.topics.court-schedule-updated:court.schedule.updated}")
    private String courtScheduleUpdatedTopic;

    public CourtEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendCourtCreated(Court court) {
        log.info("Publishing court.created event: courtId={}", court.getId());
        publishAfterCommit(courtCreatedTopic, mapCourtEvent(court));
    }

    public void sendCourtUpdated(Court court) {
        log.info("Publishing court.updated event: courtId={}", court.getId());
        publishAfterCommit(courtUpdatedTopic, mapCourtEvent(court));
    }

    public void sendCourtStatusChanged(Court court) {
        log.info("Publishing court.status.changed event: courtId={}, status={}", court.getId(), court.getStatus());
        publishAfterCommit(courtStatusChangedTopic, mapCourtEvent(court));
    }

    public void sendCourtScheduleUpdated(Court court, CourtSchedule schedule) {
        log.info("Publishing court.schedule.updated event: courtId={}, dayOfWeek={}", court.getId(), schedule.getDayOfWeek());
        publishAfterCommit(courtScheduleUpdatedTopic, mapScheduleEvent(court, schedule));
    }

    private CourtEventPayload mapCourtEvent(Court court) {
        return new CourtEventPayload(
                court.getId(),
                court.getName(),
                court.getLocation(),
                court.getSportType(),
                court.getSurfaceType(),
                court.getCapacity(),
                court.getStatus(),
                court.getCreatedAt()
        );
    }

    private CourtScheduleEventPayload mapScheduleEvent(Court court, CourtSchedule schedule) {
        return new CourtScheduleEventPayload(
                court.getId(),
                schedule.getDayOfWeek(),
                schedule.getOpenTime(),
                schedule.getCloseTime(),
                court.getSportType(),
                court.getSurfaceType(),
                court.getCapacity(),
                court.getStatus()
        );
    }

    private void publishAfterCommit(String topic, Object payload) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    kafkaTemplate.send(topic, payload);
                }
            });
        } else {
            kafkaTemplate.send(topic, payload);
        }
    }
}
