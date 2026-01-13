package com.courthub.booking.repository;

import com.courthub.booking.domain.Booking;
import com.courthub.booking.domain.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    boolean existsByTimeSlotIdAndStatus(UUID timeSlotId, BookingStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Booking> findById(UUID id);
}
