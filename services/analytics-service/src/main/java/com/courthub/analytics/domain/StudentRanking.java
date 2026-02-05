package com.courthub.analytics.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "student_rankings")
public class StudentRanking {

    @Id
    private String id;

    private LocalDate date;
    private String userId;
    private String userName;
    private String faculty;
    private int totalBookings;
    private int totalHours;
}
