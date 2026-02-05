package com.courthub.court.repository;

import com.courthub.court.domain.Court;
import com.courthub.court.domain.CourtStatus;
import com.courthub.court.domain.SportType;
import com.courthub.court.domain.SurfaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourtRepository extends JpaRepository<Court, UUID> {

    @Query("""
            SELECT c FROM Court c
            WHERE (:sportType IS NULL OR c.sportType = :sportType)
              AND (:surfaceType IS NULL OR c.surfaceType = :surfaceType)
              AND (:status IS NULL OR c.status = :status)
            """)
    List<Court> findByFilters(SportType sportType, SurfaceType surfaceType, CourtStatus status);
}
