package com.courthub.analytics.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "maintenance_metrics")
public class MaintenanceMetric {

    @Id
    private String id;
    private LocalDate date;
    private int criticalIssuesCount;
    private int highIssuesCount;
    private int mediumIssuesCount;
    private int lowIssuesCount;
    private int resolvedIssuesCount;

    public MaintenanceMetric() {}

    public MaintenanceMetric(LocalDate date, int criticalIssuesCount, int highIssuesCount,
                            int mediumIssuesCount, int lowIssuesCount, int resolvedIssuesCount) {
        this.date = date;
        this.criticalIssuesCount = criticalIssuesCount;
        this.highIssuesCount = highIssuesCount;
        this.mediumIssuesCount = mediumIssuesCount;
        this.lowIssuesCount = lowIssuesCount;
        this.resolvedIssuesCount = resolvedIssuesCount;
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

    public int getCriticalIssuesCount() {
        return criticalIssuesCount;
    }

    public void setCriticalIssuesCount(int criticalIssuesCount) {
        this.criticalIssuesCount = criticalIssuesCount;
    }

    public int getHighIssuesCount() {
        return highIssuesCount;
    }

    public void setHighIssuesCount(int highIssuesCount) {
        this.highIssuesCount = highIssuesCount;
    }

    public int getMediumIssuesCount() {
        return mediumIssuesCount;
    }

    public void setMediumIssuesCount(int mediumIssuesCount) {
        this.mediumIssuesCount = mediumIssuesCount;
    }

    public int getLowIssuesCount() {
        return lowIssuesCount;
    }

    public void setLowIssuesCount(int lowIssuesCount) {
        this.lowIssuesCount = lowIssuesCount;
    }

    public int getResolvedIssuesCount() {
        return resolvedIssuesCount;
    }

    public void setResolvedIssuesCount(int resolvedIssuesCount) {
        this.resolvedIssuesCount = resolvedIssuesCount;
    }
}
