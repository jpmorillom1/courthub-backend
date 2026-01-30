package com.courthub.analytics.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reservation_history")
public class ReservationHistory {

    @Id
    private String id;

    private String month; // e.g. "Oct"
    private int completedCount;
    private int cancelledCount;
    private int year;
}
