package com.courthub.analytics.repository;

import com.courthub.analytics.domain.ReservationHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ReservationHistoryRepository extends MongoRepository<ReservationHistory, String> {
    Optional<ReservationHistory> findByYearAndMonth(int year, String month);
}
