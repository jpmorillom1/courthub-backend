package com.courthub.analytics.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "peak_hours_metrics")
public class PeakHoursMetric {

    @Id
    private String id;
    private String dayOfWeek;
    private Map<String, Integer> hourlyBookings;
    private long totalBookings;

    public PeakHoursMetric() {}

    public PeakHoursMetric(String dayOfWeek, Map<String, Integer> hourlyBookings) {
        this.dayOfWeek = dayOfWeek;
        this.hourlyBookings = hourlyBookings;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Map<String, Integer> getHourlyBookings() {
        return hourlyBookings;
    }

    public void setHourlyBookings(Map<String, Integer> hourlyBookings) {
        this.hourlyBookings = hourlyBookings;
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(long totalBookings) {
        this.totalBookings = totalBookings;
    }
}
