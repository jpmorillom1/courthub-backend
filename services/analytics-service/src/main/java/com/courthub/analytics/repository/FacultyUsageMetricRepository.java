package com.courthub.analytics.repository;

import com.courthub.analytics.domain.FacultyUsageMetric;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyUsageMetricRepository extends MongoRepository<FacultyUsageMetric, String> {
    List<FacultyUsageMetric> findByDate(LocalDate date);
    List<FacultyUsageMetric> findByFaculty(String faculty);
    Optional<FacultyUsageMetric> findByDateAndFaculty(LocalDate date, String faculty);
}
