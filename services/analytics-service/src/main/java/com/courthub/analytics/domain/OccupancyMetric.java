package com.courthub.analytics.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Map;

@Document(collection = "occupancy_metrics")
public class OccupancyMetric {

    @Id
    private String id;
    private LocalDate date;
    private String courtId;
    private double occupancyRate;
    private int totalSlots;
    private int occupiedSlots;
    private Map<String, Integer> hourlyOccupancy; // hour -> count

    public OccupancyMetric() {}

    public OccupancyMetric(LocalDate date, String courtId, double occupancyRate, 
                          int totalSlots, int occupiedSlots, Map<String, Integer> hourlyOccupancy) {
        this.date = date;
        this.courtId = courtId;
        this.occupancyRate = occupancyRate;
        this.totalSlots = totalSlots;
        this.occupiedSlots = occupiedSlots;
        this.hourlyOccupancy = hourlyOccupancy;
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

    public String getCourtId() {
        return courtId;
    }

    public void setCourtId(String courtId) {
        this.courtId = courtId;
    }

    public double getOccupancyRate() {
        return occupancyRate;
    }

    public void setOccupancyRate(double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(int totalSlots) {
        this.totalSlots = totalSlots;
    }

    public int getOccupiedSlots() {
        return occupiedSlots;
    }

    public void setOccupiedSlots(int occupiedSlots) {
        this.occupiedSlots = occupiedSlots;
    }

    public Map<String, Integer> getHourlyOccupancy() {
        return hourlyOccupancy;
    }

    public void setHourlyOccupancy(Map<String, Integer> hourlyOccupancy) {
        this.hourlyOccupancy = hourlyOccupancy;
    }
}
