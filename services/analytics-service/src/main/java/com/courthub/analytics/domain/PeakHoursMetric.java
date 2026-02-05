package com.courthub.analytics.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Map;

@Document(collection = "peak_hours_metrics")
public class PeakHoursMetric {

    @Id
    private String id;
    private LocalDate date;
    private Map<String, Integer> hourlyBookings; // key: "HH:00"

    public PeakHoursMetric() {}

    public PeakHoursMetric(LocalDate date, Map<String, Integer> hourlyBookings) {
        this.date = date;
        this.hourlyBookings = hourlyBookings;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Map<String, Integer> getHourlyBookings() {
        return hourlyBookings;
    }

    public void setHourlyBookings(Map<String, Integer> hourlyBookings) {
        this.hourlyBookings = hourlyBookings;
    }
}
