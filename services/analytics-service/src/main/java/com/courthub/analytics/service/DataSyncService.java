package com.courthub.analytics.service;

import com.courthub.analytics.client.BookingServiceFeignClient;
import com.courthub.analytics.client.CourtServiceFeignClient;
import com.courthub.analytics.client.UserServiceFeignClient;
import com.courthub.common.dto.analytics.BookingInternalDTO;
import com.courthub.common.dto.analytics.CourtIssueInternalDTO;
import com.courthub.common.dto.analytics.UserInternalDTO;
import com.courthub.analytics.domain.FacultyUsageMetric;
import com.courthub.analytics.domain.MaintenanceMetric;
import com.courthub.analytics.domain.OccupancyMetric;
import com.courthub.analytics.domain.PeakHoursMetric;
import com.courthub.analytics.domain.ReservationHistory;
import com.courthub.analytics.domain.StudentRanking;
import com.courthub.analytics.repository.FacultyUsageMetricRepository;
import com.courthub.analytics.repository.MaintenanceMetricRepository;
import com.courthub.analytics.repository.OccupancyMetricRepository;
import com.courthub.analytics.repository.PeakHoursMetricRepository;
import com.courthub.analytics.repository.ReservationHistoryRepository;
import com.courthub.analytics.repository.StudentRankingRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
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
public class DataSyncService {

    private final BookingServiceFeignClient bookingServiceFeignClient;
    private final UserServiceFeignClient userServiceFeignClient;
    private final CourtServiceFeignClient courtServiceFeignClient;
    private final OccupancyMetricRepository occupancyMetricRepository;
    private final FacultyUsageMetricRepository facultyUsageMetricRepository;
    private final PeakHoursMetricRepository peakHoursMetricRepository;
    private final MaintenanceMetricRepository maintenanceMetricRepository;
    private final StudentRankingRepository studentRankingRepository;
    private final ReservationHistoryRepository reservationHistoryRepository;

    private static final Map<String, String> FACULTY_COLORS = Map.ofEntries(
            Map.entry("INGENIERÍA Y CIENCIAS APLICADAS", "#00458d"), // FICA
            Map.entry("CIENCIAS MÉDICAS", "#c19a32"),
            Map.entry("JURISPRUDENCIA, CIENCIAS POLÍTICAS Y SOCIALES", "#d41d51"),
            Map.entry("CIENCIAS ECONÓMICAS", "#003d6b"),
            Map.entry("CIENCIAS", "#6ba3d4"),
            Map.entry("CIENCIAS ADMINISTRATIVAS", "#8b4513"),
            Map.entry("FILOSOFÍA, LETRAS Y CIENCIAS DE LA EDUCACIÓN", "#228b22"),
            Map.entry("CIENCIAS SOCIALES Y HUMANAS", "#9370db"),
            Map.entry("ARQUITECTURA Y URBANISMO", "#5f9ea0"),
            Map.entry("ARTES", "#e9967a"),
            Map.entry("CIENCIAS AGRÍCOLAS", "#556b2f"),
            Map.entry("CIENCIAS BIOLÓGICAS", "#2e8b57"),
            Map.entry("CIENCIAS DE LA DISCAPACIDAD, ATENCIÓN PREHOSPITALARIA Y DESASTRES", "#4682b4"),
            Map.entry("CIENCIAS PSICOLÓGICAS", "#db7093"),
            Map.entry("CIENCIAS QUÍMICAS", "#7b68ee"),
            Map.entry("COMUNICACIÓN SOCIAL", "#cd5c5c"),
            Map.entry("CULTURA FÍSICA", "#ffa500"),
            Map.entry("INGENIERÍA EN GEOLOGÍA, MINAS, PETRÓLEOS Y AMBIENTAL", "#2f4f4f"), // FIGEMPA
            Map.entry("INGENIERÍA QUÍMICA", "#008080"),
            Map.entry("MEDICINA VETERINARIA Y ZOOTECNIA", "#8fbc8f"),
            Map.entry("ODONTOLOGÍA", "#b0c4de")
    );
    @Scheduled(cron = "0 0 * * * *") // Cada hora
    public void syncAnalyticsData() {
        long start = System.currentTimeMillis();
        log.info("Starting analytics data sync");
        try {
            List<BookingInternalDTO> bookings = extractBookings();
            List<UserInternalDTO> users = extractUsers();
            List<CourtIssueInternalDTO> courtIssues = extractCourtIssues();

            log.info("Analytics data extracted: bookings={}, users={}, courtIssues={}",
                    bookings.size(), users.size(), courtIssues.size());

            Map<String, UserInternalDTO> userMap = users.stream()
                .collect(Collectors.toMap(UserInternalDTO::id, u -> u));

            processOccupancyMetrics(bookings);
            processPeakHours(bookings);
            processFacultyUsage(bookings, userMap);
            processMaintenanceMetrics(courtIssues);
            processStudentRanking(bookings, userMap);
            processReservationHistory(bookings);

            long durationMs = System.currentTimeMillis() - start;
            log.info("Analytics data sync completed successfully: durationMs={}", durationMs);
        } catch (Exception e) {
            log.error("Error during analytics data sync", e);
        }
    }

    private List<BookingInternalDTO> extractBookings() {
        try {
            List<BookingInternalDTO> bookings = bookingServiceFeignClient.getAllBookings();
            return bookings != null ? bookings : Collections.emptyList();
        } catch (FeignException e) {
            log.warn("Failed to extract bookings from booking-service", e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error extracting bookings from booking-service", e);
            return Collections.emptyList();
        }
    }

    private List<UserInternalDTO> extractUsers() {
        try {
            List<UserInternalDTO> users = userServiceFeignClient.getAllUsers();
            return users != null ? users : Collections.emptyList();
        } catch (FeignException e) {
            log.warn("Failed to extract users from user-service", e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error extracting users from user-service", e);
            return Collections.emptyList();
        }
    }

    private List<CourtIssueInternalDTO> extractCourtIssues() {
        try {
            List<CourtIssueInternalDTO> issues = courtServiceFeignClient.getAllCourtIssues();
            return issues != null ? issues : Collections.emptyList();
        } catch (FeignException e) {
            log.warn("Failed to extract court issues from court-service", e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error extracting court issues from court-service", e);
            return Collections.emptyList();
        }
    }

    private void processOccupancyMetrics(List<BookingInternalDTO> bookings) {
        Map<String, List<BookingInternalDTO>> byDateAndCourt = bookings.stream()
                .collect(Collectors.groupingBy(b -> b.date().toString() + "|" + b.courtId()));

        for (Map.Entry<String, List<BookingInternalDTO>> entry : byDateAndCourt.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            LocalDate date = LocalDate.parse(parts[0]); // Ahora parts[0] será "2026-01-29" completo
            String courtId = parts[1];
            List<BookingInternalDTO> courtBookings = entry.getValue();

            long occupiedSlots = courtBookings.stream()
                .filter(b -> "CONFIRMED".equals(b.status()))
                .count();

            int totalSlots = 8; // Assuming 8 slots per day (8am-4pm with 1-hour slots)
            double occupancyRate = (double) occupiedSlots / totalSlots;

            Map<String, Integer> hourlyOccupancy = courtBookings.stream()
                .filter(b -> "CONFIRMED".equals(b.status()))
                .collect(Collectors.groupingBy(
                    b -> String.format("%02d:00", b.startTime().getHour()),
                    Collectors.summingInt(b -> 1)
                ));

            OccupancyMetric metric = occupancyMetricRepository
                .findByDateAndCourtId(date, courtId)
                .orElse(new OccupancyMetric());

            metric.setDate(date);
            metric.setCourtId(courtId);
            metric.setOccupancyRate(occupancyRate);
            metric.setTotalSlots(totalSlots);
            metric.setOccupiedSlots((int) occupiedSlots);
            metric.setHourlyOccupancy(hourlyOccupancy);

            occupancyMetricRepository.save(metric);
        }
    }

    private void processPeakHours(List<BookingInternalDTO> bookings) {
        Map<String, Integer> hourlyBookings = new HashMap<>();
        for (BookingInternalDTO booking : bookings) {
            if ("CONFIRMED".equals(booking.status())) {
                String hour = String.format("%02d:00", booking.startTime().getHour());
                hourlyBookings.merge(hour, 1, Integer::sum);
            }
        }

        LocalDate today = LocalDate.now();
        PeakHoursMetric metric = peakHoursMetricRepository
            .findByDate(today)
            .orElse(new PeakHoursMetric());

        metric.setDate(today);
        metric.setHourlyBookings(hourlyBookings);

        peakHoursMetricRepository.save(metric);
    }

    private void processFacultyUsage(List<BookingInternalDTO> bookings, Map<String, UserInternalDTO> userMap) {
        Map<String, Integer> facultyBookingCounts = new HashMap<>();
        
        for (BookingInternalDTO booking : bookings) {
            if ("CONFIRMED".equals(booking.status())) {
                UserInternalDTO user = userMap.get(booking.userId());
                if (user != null) {
                    String faculty = user.faculty();
                    facultyBookingCounts.merge(faculty, 1, Integer::sum);
                }
            }
        }

        LocalDate today = LocalDate.now();
        for (Map.Entry<String, Integer> entry : facultyBookingCounts.entrySet()) {
            String faculty = entry.getKey();
            int count = entry.getValue();
            String color = FACULTY_COLORS.getOrDefault(faculty, "#808080");
            
            // Calculate average occupancy for this faculty
            double avgOccupancy = (double) count / 100; // Simplified calculation

            FacultyUsageMetric metric = facultyUsageMetricRepository
                .findByDateAndFaculty(today, faculty)
                .orElse(new FacultyUsageMetric());

            metric.setDate(today);
            metric.setFaculty(faculty);
            metric.setBookingCount(count);
            metric.setAverageOccupancyRate(avgOccupancy);
            metric.setColor(color);

            facultyUsageMetricRepository.save(metric);
        }
    }

    private void processMaintenanceMetrics(List<CourtIssueInternalDTO> courtIssues) {
        int criticalCount = 0;
        int highCount = 0;
        int mediumCount = 0;
        int lowCount = 0;
        int resolvedCount = 0;

        for (CourtIssueInternalDTO issue : courtIssues) {
            if ("RESOLVED".equals(issue.status())) {
                resolvedCount++;
            } else {
                switch (issue.severity()) {
                    case "CRITICAL" -> criticalCount++;
                    case "HIGH" -> highCount++;
                    case "MEDIUM" -> mediumCount++;
                    case "LOW" -> lowCount++;
                }
            }
        }

        LocalDate today = LocalDate.now();
        MaintenanceMetric metric = maintenanceMetricRepository
            .findByDate(today)
            .orElse(new MaintenanceMetric());

        metric.setDate(today);
        metric.setCriticalIssuesCount(criticalCount);
        metric.setHighIssuesCount(highCount);
        metric.setMediumIssuesCount(mediumCount);
        metric.setLowIssuesCount(lowCount);
        metric.setResolvedIssuesCount(resolvedCount);

        maintenanceMetricRepository.save(metric);
    }

    private void processStudentRanking(List<BookingInternalDTO> bookings, Map<String, UserInternalDTO> userMap) {
        if (bookings.isEmpty()) {
            log.debug("Skipping student ranking: no bookings available");
            return;
        }

        Map<String, Long> totalByUser = bookings.stream()
            .collect(Collectors.groupingBy(BookingInternalDTO::userId, Collectors.counting()));

        Map<String, Long> confirmedByUser = bookings.stream()
            .filter(b -> "CONFIRMED".equals(b.status()))
            .collect(Collectors.groupingBy(BookingInternalDTO::userId, Collectors.counting()));

        LocalDate today = LocalDate.now();
        studentRankingRepository.deleteByDate(today);

        List<StudentRanking> topRanked = confirmedByUser.entrySet().stream()
            .map(entry -> {
                String userId = entry.getKey();
                long confirmedCount = entry.getValue();
                long totalCount = totalByUser.getOrDefault(userId, 0L);

                UserInternalDTO user = userMap.get(userId);
                String userName = user != null ? user.name() : "Unknown";
                String faculty = user != null ? user.faculty() : "Unknown";

                int totalBookings = (int) confirmedCount;
                int totalHours = (int) confirmedCount; // 1 hora por reserva

                return new StudentRanking(
                    null,
                    today,
                    userId,
                    userName,
                    faculty,
                    totalBookings,
                    totalHours
                );
            })
            .sorted(Comparator.comparingInt(StudentRanking::getTotalBookings).reversed())
            .limit(5)
            .collect(Collectors.toList());

        studentRankingRepository.saveAll(topRanked);
    }

    private void processReservationHistory(List<BookingInternalDTO> bookings) {
        if (bookings.isEmpty()) {
            log.debug("Skipping reservation history: no bookings available");
            return;
        }

        Map<YearMonth, int[]> countsByMonth = new HashMap<>();

        for (BookingInternalDTO booking : bookings) {
            if (booking.date() == null) {
                continue;
            }
            YearMonth yearMonth = YearMonth.from(booking.date());
            int[] counts = countsByMonth.computeIfAbsent(yearMonth, ym -> new int[2]);

            String status = booking.status();
            if ("CONFIRMED".equals(status) || "COMPLETED".equals(status)) {
                counts[0]++;
            } else if ("CANCELLED".equals(status)) {
                counts[1]++;
            }
        }

        for (Map.Entry<YearMonth, int[]> entry : countsByMonth.entrySet()) {
            YearMonth yearMonth = entry.getKey();
            int completedCount = entry.getValue()[0];
            int cancelledCount = entry.getValue()[1];

            String month = yearMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            int year = yearMonth.getYear();

            ReservationHistory history = reservationHistoryRepository
                .findByYearAndMonth(year, month)
                .orElse(new ReservationHistory(null, month, 0, 0, year));

            history.setCompletedCount(completedCount);
            history.setCancelledCount(cancelledCount);

            reservationHistoryRepository.save(history);
        }
    }
}
