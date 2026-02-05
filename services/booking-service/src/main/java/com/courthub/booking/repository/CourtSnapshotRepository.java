package com.courthub.booking.repository;

import com.courthub.booking.domain.CourtSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CourtSnapshotRepository extends JpaRepository<CourtSnapshot, UUID> {
}
