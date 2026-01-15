package com.courthub.realtime.service;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseService {

    private final FirebaseDatabase firebaseDatabase;


    public void updateAvailability(UUID courtId, LocalDate date, LocalTime startTime, String status) {
        try {
            String firebaseKey = formatTimeForFirebase(startTime);
            String path = String.format("availability/%s/%s/%s", courtId, date, firebaseKey);

            Map<String, Object> update = new HashMap<>();
            update.put("status", status);
            update.put("updatedAt", ServerValue.TIMESTAMP);

            DatabaseReference ref = firebaseDatabase.getReference(path);

            ApiFuture<Void> future = ref.updateChildrenAsync(update);

            ApiFutures.addCallback(future, new ApiFutureCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    log.info("Successfully updated Firebase: {} -> {}", path, status);
                }

                @Override
                public void onFailure(Throwable t) {
                    log.error("Failed to update Firebase at path: {}", path, t);
                }
            }, MoreExecutors.directExecutor());

        } catch (Exception e) {
            log.error("Unexpected error updating availability for court {} on date {}", courtId, date, e);
        }
    }


    private String formatTimeForFirebase(LocalTime time) {
        return String.format("%02d-%02d", time.getHour(), time.getMinute());
    }
}