package com.courthub.court.repository;

import com.courthub.court.domain.CourtSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourtScheduleRepository extends JpaRepository<CourtSchedule, UUID> {

    Optional<CourtSchedule> findByCourtIdAndDayOfWeek(UUID courtId, int dayOfWeek);

    boolean existsByCourtIdAndDayOfWeek(UUID courtId, int dayOfWeek);

    List<CourtSchedule> findByCourtId(UUID courtId);

    List<CourtSchedule> findByCourtIdIn(List<UUID> courtIds);

    @Query("SELECT cs FROM CourtSchedule cs WHERE cs.courtId = :courtId " +
            "AND cs.dayOfWeek >= :startDay AND cs.dayOfWeek <= :endDay")
    List<CourtSchedule> findByCourtIdAndDateRange(
            @Param("courtId") UUID courtId,
            @Param("startDay") int startDay,
            @Param("endDay") int endDay
    );}
