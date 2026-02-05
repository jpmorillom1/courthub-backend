package com.courthub.notification.repository;

import com.courthub.notification.domain.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends MongoRepository<NotificationLog, String> {


    List<NotificationLog> findByUserIdOrderByTimestampDesc(UUID userId);

    long countByUserId(UUID userId);
}
