package com.courthub.court.service;

import com.courthub.court.domain.Court;
import com.courthub.court.domain.CourtSchedule;
import com.courthub.court.domain.CourtStatus;
import com.courthub.court.event.CourtEventProducer;
import com.courthub.court.repository.CourtRepository;
import com.courthub.court.repository.CourtScheduleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class WeeklySchedulerService {

    private final CourtRepository courtRepository;
    private final CourtScheduleRepository courtScheduleRepository;
    private final CourtEventProducer courtEventProducer;

    private static final LocalTime DEFAULT_OPEN_TIME = LocalTime.of(8, 0);
    private static final LocalTime DEFAULT_CLOSE_TIME = LocalTime.of(20, 0);

    public WeeklySchedulerService(CourtRepository courtRepository,
                                   CourtScheduleRepository courtScheduleRepository,
                                   CourtEventProducer courtEventProducer) {
        this.courtRepository = courtRepository;
        this.courtScheduleRepository = courtScheduleRepository;
        this.courtEventProducer = courtEventProducer;
    }

    @Scheduled(cron = "0 0 0 * * MON")
 //@Scheduled(cron = "0 37 3 * * *")
    @Transactional
    public void createWeeklySchedules() {
        long start = System.currentTimeMillis();
        log.info("Starting weekly schedule creation job");
        try {
            List<Court> activeCourts = courtRepository.findByFilters(null, null, CourtStatus.ACTIVE);

            if (activeCourts.isEmpty()) {
                log.info("No active courts found");
                return;
            }

            log.info("Found {} active courts", activeCourts.size());

            LocalDate today = LocalDate.now();
            LocalDate nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            LocalDate nextSunday = nextMonday.plusDays(6);
            log.info("Creating schedules for the week of {} to {}", nextMonday, nextSunday);

            int totalSchedulesCreated = 0;

            for (Court court : activeCourts) {
                List<CourtSchedule> newSchedules = createSchedulesForCourt(court, nextMonday, nextSunday);
                totalSchedulesCreated += newSchedules.size();

                for (CourtSchedule schedule : newSchedules) {
                    courtEventProducer.sendCourtScheduleUpdated(court, schedule);
                }
            }
            long durationMs = System.currentTimeMillis() - start;
            log.info("Weekly schedule creation completed: created={}, durationMs={}", totalSchedulesCreated, durationMs);
        } catch (Exception e) {
            log.error("Error during weekly schedule creation", e);
            throw e;
        }
    }


    private List<CourtSchedule> createSchedulesForCourt(Court court, LocalDate weekStart, LocalDate weekEnd) {
        List<CourtSchedule> createdSchedules = new ArrayList<>();
        LocalDate currentDate = weekStart;

        while (!currentDate.isAfter(weekEnd)) {
            int dayOfWeek = currentDate.getDayOfWeek().getValue();

            if (!courtScheduleRepository.existsByCourtIdAndDayOfWeek(court.getId(), dayOfWeek)) {
                CourtSchedule schedule = new CourtSchedule();
                schedule.setCourtId(court.getId());
                schedule.setDayOfWeek(dayOfWeek);
                schedule.setOpenTime(DEFAULT_OPEN_TIME);
                schedule.setCloseTime(DEFAULT_CLOSE_TIME);

                CourtSchedule saved = courtScheduleRepository.save(schedule);
                createdSchedules.add(saved);

                log.debug("Created schedule for court {} on day {}", court.getId(), dayOfWeek);
            } else {
                log.debug("Schedule already exists for court {} on day {}", court.getId(), dayOfWeek);
            }

            currentDate = currentDate.plusDays(1);
        }

        return createdSchedules;
    }


    @Transactional
    public void createSchedulesForSpecificWeek(LocalDate weekStart) {
        long start = System.currentTimeMillis();
        log.info("Manually creating schedules for week starting on {}", weekStart);

        List<Court> activeCourts = courtRepository.findByFilters(null, null, CourtStatus.ACTIVE);
        LocalDate weekEnd = weekStart.plusDays(6);
        int totalSchedulesCreated = 0;

        for (Court court : activeCourts) {
            List<CourtSchedule> newSchedules = createSchedulesForCourt(court, weekStart, weekEnd);
            totalSchedulesCreated += newSchedules.size();

            for (CourtSchedule schedule : newSchedules) {
                courtEventProducer.sendCourtScheduleUpdated(court, schedule);
            }
        }
        long durationMs = System.currentTimeMillis() - start;
        log.info("Manual schedule creation completed: created={}, durationMs={}", totalSchedulesCreated, durationMs);
    }
}
