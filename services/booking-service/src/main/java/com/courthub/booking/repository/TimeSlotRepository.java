package com.courthub.booking.repository;

import com.courthub.booking.domain.TimeSlot;
import com.courthub.common.dto.enums.TimeSlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {

    List<TimeSlot> findByCourtIdAndDateAndStatusOrderByStartTime(UUID courtId, LocalDate date, TimeSlotStatus status);

  //  @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TimeSlot> findByCourtIdAndDateAndStartTime(UUID courtId, LocalDate date, LocalTime startTime);

   // @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TimeSlot> findById(UUID id);

    boolean existsByCourtIdAndDateAndStartTime(UUID courtId, LocalDate date, LocalTime startTime);

    @Modifying
    @Query("delete from TimeSlot ts where ts.courtId = :courtId and ts.date = :date and ts.status = :status")
    void deleteByCourtIdAndDateAndStatus(UUID courtId, LocalDate date, TimeSlotStatus status);

    List<TimeSlot> findByDate(LocalDate date);

    @Modifying
    @Query("delete from TimeSlot ts where ts.date < :currentDate and ts.status = :status " +
           "and not exists (select 1 from Booking b where b.timeSlotId = ts.id)")
    void deleteOldUnusedSlots(LocalDate currentDate, TimeSlotStatus status);
}
