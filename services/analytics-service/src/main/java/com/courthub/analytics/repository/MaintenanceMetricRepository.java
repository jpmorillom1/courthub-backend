package com.courthub.analytics.repository;

import com.courthub.analytics.domain.MaintenanceMetric;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MaintenanceMetricRepository extends MongoRepository<MaintenanceMetric, String> {
    Optional<MaintenanceMetric> findByDate(LocalDate date);
}
