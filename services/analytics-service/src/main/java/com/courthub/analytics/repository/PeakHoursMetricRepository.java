package com.courthub.analytics.repository;

import com.courthub.analytics.domain.PeakHoursMetric;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PeakHoursMetricRepository extends MongoRepository<PeakHoursMetric, String> {
    Optional<PeakHoursMetric> findByDayOfWeek(String dayOfWeek);
}
