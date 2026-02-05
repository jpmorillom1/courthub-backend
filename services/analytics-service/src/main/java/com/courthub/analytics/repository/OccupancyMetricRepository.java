package com.courthub.analytics.repository;

import com.courthub.analytics.domain.OccupancyMetric;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OccupancyMetricRepository extends MongoRepository<OccupancyMetric, String> {
    List<OccupancyMetric> findByDate(LocalDate date);
    List<OccupancyMetric> findByCourtId(String courtId);
    Optional<OccupancyMetric> findByDateAndCourtId(LocalDate date, String courtId);
}
