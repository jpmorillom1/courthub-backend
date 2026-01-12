package com.courthub.court.repository;

import com.courthub.court.domain.CourtSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourtScheduleRepository extends JpaRepository<CourtSchedule, UUID> {

    Optional<CourtSchedule> findByCourtIdAndDayOfWeek(UUID courtId, int dayOfWeek);

    boolean existsByCourtIdAndDayOfWeek(UUID courtId, int dayOfWeek);

    List<CourtSchedule> findByCourtId(UUID courtId);

    List<CourtSchedule> findByCourtIdIn(List<UUID> courtIds);
}
