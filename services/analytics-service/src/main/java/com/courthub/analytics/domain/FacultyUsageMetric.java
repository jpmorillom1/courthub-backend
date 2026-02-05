package com.courthub.analytics.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "faculty_usage_metrics")
public class FacultyUsageMetric {

    @Id
    private String id;
    private LocalDate date;
    private String faculty;
    private int bookingCount;
    private double averageOccupancyRate;
    private String color; // Color específico para la facultad

    public FacultyUsageMetric() {}

    public FacultyUsageMetric(LocalDate date, String faculty, int bookingCount, 
                             double averageOccupancyRate, String color) {
        this.date = date;
        this.faculty = faculty;
        this.bookingCount = bookingCount;
        this.averageOccupancyRate = averageOccupancyRate;
        this.color = color;
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

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public int getBookingCount() {
        return bookingCount;
    }

    public void setBookingCount(int bookingCount) {
        this.bookingCount = bookingCount;
    }

    public double getAverageOccupancyRate() {
        return averageOccupancyRate;
    }

    public void setAverageOccupancyRate(double averageOccupancyRate) {
        this.averageOccupancyRate = averageOccupancyRate;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
