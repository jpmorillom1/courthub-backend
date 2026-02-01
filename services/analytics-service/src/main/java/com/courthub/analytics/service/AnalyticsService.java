package com.courthub.analytics.service;

import com.courthub.analytics.domain.FacultyUsageMetric;
import com.courthub.analytics.domain.MaintenanceMetric;
import com.courthub.analytics.domain.OccupancyMetric;
import com.courthub.analytics.domain.PeakHoursMetric;
import com.courthub.analytics.domain.ReservationHistory;
import com.courthub.analytics.domain.StudentRanking;
import com.courthub.analytics.dto.DashboardResponse;
import com.courthub.analytics.dto.FacultyUsageResponse;
import com.courthub.analytics.dto.HeatmapResponse;
import com.courthub.analytics.dto.KPIsResponse;
import com.courthub.analytics.dto.ReservationHistoryResponse;
import com.courthub.analytics.dto.StudentRankingResponse;
import com.courthub.analytics.repository.FacultyUsageMetricRepository;
import com.courthub.analytics.repository.MaintenanceMetricRepository;
import com.courthub.analytics.repository.OccupancyMetricRepository;
import com.courthub.analytics.repository.PeakHoursMetricRepository;
import com.courthub.analytics.repository.ReservationHistoryRepository;
import com.courthub.analytics.repository.StudentRankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OccupancyMetricRepository occupancyMetricRepository;
    private final FacultyUsageMetricRepository facultyUsageMetricRepository;
    private final PeakHoursMetricRepository peakHoursMetricRepository;
    private final MaintenanceMetricRepository maintenanceMetricRepository;
    private final StudentRankingRepository studentRankingRepository;
    private final ReservationHistoryRepository reservationHistoryRepository;

    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();
        log.info("Building analytics dashboard for date={}", today);

        KPIsResponse kpis = buildKPIs(today);

        HeatmapResponse heatmap = buildHeatmap(today);

        List<FacultyUsageResponse> facultyUsage = buildFacultyUsage(today);

        List<StudentRankingResponse> topActiveStudents = buildStudentRanking();

        List<ReservationHistoryResponse> reservationsHistory = buildReservationHistory();

        DashboardResponse response = new DashboardResponse(kpis, heatmap, facultyUsage, topActiveStudents, reservationsHistory);
        log.info("Analytics dashboard built successfully: facultyUsageCount={}, topStudentsCount={}, historyCount={}",
            facultyUsage.size(), topActiveStudents.size(), reservationsHistory.size());
        return response;
    }

    private KPIsResponse buildKPIs(LocalDate date) {
        List<OccupancyMetric> occupancyMetrics = occupancyMetricRepository.findByDate(date);
        double avgOccupancyRate = occupancyMetrics.stream()
            .mapToDouble(OccupancyMetric::getOccupancyRate)
            .average()
            .orElse(0.0);

        List<FacultyUsageMetric> facultyMetrics = facultyUsageMetricRepository.findByDate(date);
        int totalStudents = facultyMetrics.stream()
            .mapToInt(FacultyUsageMetric::getBookingCount)
            .sum();

        MaintenanceMetric maintenanceMetric = maintenanceMetricRepository
            .findByDate(date)
            .orElse(new MaintenanceMetric(date, 0, 0, 0, 0, 0));

        int totalMaintenanceIssues = maintenanceMetric.getCriticalIssuesCount()
            + maintenanceMetric.getHighIssuesCount()
            + maintenanceMetric.getMediumIssuesCount()
            + maintenanceMetric.getLowIssuesCount();

        return new KPIsResponse(
            avgOccupancyRate * 100,
            totalStudents,
            totalMaintenanceIssues,
            maintenanceMetric.getCriticalIssuesCount(),
            maintenanceMetric.getResolvedIssuesCount()
        );
    }

    private HeatmapResponse buildHeatmap(LocalDate date) {
        Optional<PeakHoursMetric> peakMetric = peakHoursMetricRepository.findByDate(date);

        Map<String, Map<String, Integer>> dayHourMatrix = new HashMap<>();
        Map<String, Integer> hourCounts = new HashMap<>();

        if (peakMetric.isPresent() && peakMetric.get().getHourlyBookings() != null) {
            hourCounts.putAll(peakMetric.get().getHourlyBookings());
        }

        String dayOfWeek = date.getDayOfWeek().toString();
        dayHourMatrix.put(dayOfWeek, hourCounts);

        return new HeatmapResponse(dayHourMatrix);
    }

    private List<FacultyUsageResponse> buildFacultyUsage(LocalDate date) {
        List<FacultyUsageMetric> metrics = facultyUsageMetricRepository.findByDate(date);
        
        return metrics.stream()
            .map(m -> new FacultyUsageResponse(
                m.getFaculty(),
                m.getBookingCount(),
                m.getAverageOccupancyRate() * 100,
                m.getColor()
            ))
            .sorted(Comparator.comparingInt(FacultyUsageResponse::bookingCount).reversed())
            .collect(Collectors.toList());
    }

    private List<StudentRankingResponse> buildStudentRanking() {
        Optional<StudentRanking> latest = studentRankingRepository.findTopByOrderByDateDesc();
        if (latest.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate date = latest.get().getDate();
        List<StudentRanking> rankings = studentRankingRepository.findByDate(date);

        return rankings.stream()
            .sorted(Comparator.comparingInt(StudentRanking::getTotalBookings).reversed())
            .map(r -> new StudentRankingResponse(
                r.getUserId(),
                r.getUserName(),
                r.getFaculty(),
                r.getTotalBookings(),
                r.getTotalHours(),
                r.getTotalBookings() > 0 ? "100%" : "0%"
            ))
            .collect(Collectors.toList());
    }

    private List<ReservationHistoryResponse> buildReservationHistory() {
        List<ReservationHistoryResponse> history = new ArrayList<>();
        YearMonth current = YearMonth.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth target = current.minusMonths(i);
            String month = target.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            int year = target.getYear();

            ReservationHistory record = reservationHistoryRepository
                .findByYearAndMonth(year, month)
                .orElse(new ReservationHistory(null, month, 0, 0, year));

            history.add(new ReservationHistoryResponse(
                record.getMonth(),
                record.getCompletedCount(),
                record.getCancelledCount(),
                record.getYear()
            ));
        }

        return history;
    }
}
