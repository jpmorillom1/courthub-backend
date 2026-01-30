package com.courthub.analytics.repository;

import com.courthub.analytics.domain.StudentRanking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudentRankingRepository extends MongoRepository<StudentRanking, String> {
    List<StudentRanking> findByDate(LocalDate date);
    void deleteByDate(LocalDate date);
    Optional<StudentRanking> findTopByOrderByDateDesc();
}
