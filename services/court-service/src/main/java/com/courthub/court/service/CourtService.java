package com.courthub.court.service;

import com.courthub.common.exception.BusinessException;
import com.courthub.common.exception.NotFoundException;
import com.courthub.court.domain.Court;
import com.courthub.court.domain.CourtSchedule;
import com.courthub.court.domain.CourtStatus;
import com.courthub.court.domain.SportType;
import com.courthub.court.domain.SurfaceType;
import com.courthub.court.dto.CourtRequestDto;
import com.courthub.court.dto.CourtResponseDto;
import com.courthub.court.dto.CourtScheduleRequestDto;
import com.courthub.court.dto.CourtScheduleResponseDto;
import com.courthub.court.dto.CourtStatusUpdateDto;
import com.courthub.court.event.CourtEventProducer;
import com.courthub.court.repository.CourtRepository;
import com.courthub.court.repository.CourtScheduleRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CourtService {

    private final CourtRepository courtRepository;
    private final CourtScheduleRepository scheduleRepository;
    private final CourtEventProducer eventProducer;
    private final CourtIssueService courtIssueService;

    public CourtService(CourtRepository courtRepository,
                        CourtScheduleRepository scheduleRepository,
                        CourtEventProducer eventProducer,
                        CourtIssueService courtIssueService) {
        this.courtRepository = courtRepository;
        this.scheduleRepository = scheduleRepository;
        this.eventProducer = eventProducer;
        this.courtIssueService = courtIssueService;
    }

    @Transactional
    @CacheEvict(value = "courts", allEntries = true)
    public CourtResponseDto createCourt(CourtRequestDto request) {
        validateCapacity(request.getCapacity());

        Court court = new Court();
        court.setName(request.getName());
        court.setLocation(request.getLocation());
        court.setSportType(request.getSportType());
        court.setSurfaceType(request.getSurfaceType());
        court.setCapacity(request.getCapacity());
        court.setVideoUrl(request.getVideoUrl());
        court.setStatus(CourtStatus.ACTIVE);

        Court savedCourt = courtRepository.save(court);
        eventProducer.sendCourtCreated(savedCourt);

        return toCourtResponse(savedCourt, Collections.emptyList());
    }

    @Cacheable(value = "courts", key = "T(String).format('list-%s-%s-%s', #sportType, #surfaceType, #status)")
    public List<CourtResponseDto> listCourts(SportType sportType, SurfaceType surfaceType, CourtStatus status) {
        List<Court> courts = courtRepository.findByFilters(sportType, surfaceType, status);
        if (courts.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> courtIds = courts.stream()
                .map(Court::getId)
                .collect(Collectors.toList());

        Map<UUID, List<CourtSchedule>> schedules = scheduleRepository.findByCourtIdIn(courtIds).stream()
                .collect(Collectors.groupingBy(CourtSchedule::getCourtId));

        return courts.stream()
                .map(court -> toCourtResponse(court, schedules.getOrDefault(court.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    @Cacheable(value = "courts", key = "#id")
    public CourtResponseDto getCourt(UUID id) {
        Court court = findCourtOrThrow(id);
        List<CourtSchedule> schedules = scheduleRepository.findByCourtId(id);
        return toCourtResponse(court, schedules);
    }

    @Transactional
    @CacheEvict(value = "courts", allEntries = true)
    public CourtResponseDto updateStatus(UUID id, CourtStatusUpdateDto request) {
        if (request.getStatus() == null) {
            throw new BusinessException("Status is required");
        }

        Court court = findCourtOrThrow(id);
        court.setStatus(request.getStatus());

        Court saved = courtRepository.save(court);
        eventProducer.sendCourtStatusChanged(saved);
        eventProducer.sendCourtUpdated(saved);

        List<CourtSchedule> schedules = scheduleRepository.findByCourtId(id);
        return toCourtResponse(saved, schedules);
    }

    @Transactional
    @CacheEvict(value = "courts", allEntries = true)
    public CourtScheduleResponseDto upsertSchedule(UUID courtId, CourtScheduleRequestDto request) {
        Court court = findCourtOrThrow(courtId);
        validateScheduleTimes(request.getOpenTime(), request.getCloseTime());

        CourtSchedule schedule = scheduleRepository.findByCourtIdAndDayOfWeek(courtId, request.getDayOfWeek())
                .orElseGet(CourtSchedule::new);

        schedule.setCourtId(courtId);
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setOpenTime(request.getOpenTime());
        schedule.setCloseTime(request.getCloseTime());

        CourtSchedule saved = scheduleRepository.save(schedule);
        eventProducer.sendCourtScheduleUpdated(court, saved);
        eventProducer.sendCourtUpdated(court);

        return toScheduleResponse(saved);
    }

    private Court findCourtOrThrow(UUID id) {
        return courtRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Court", id));
    }

    private void validateCapacity(int capacity) {
        if (capacity <= 0) {
            throw new BusinessException("Capacity must be greater than zero");
        }
    }

    private void validateScheduleTimes(LocalTime openTime, LocalTime closeTime) {
        if (openTime == null || closeTime == null) {
            throw new BusinessException("Open and close time are required");
        }
        if (!openTime.isBefore(closeTime)) {
            throw new BusinessException("Open time must be before close time");
        }
    }

    private CourtResponseDto toCourtResponse(Court court, List<CourtSchedule> schedules) {
        List<CourtScheduleResponseDto> scheduleDtos = schedules.stream()
                .map(this::toScheduleResponse)
                .collect(Collectors.toList());

        return new CourtResponseDto(
                court.getId(),
                court.getName(),
                court.getLocation(),
                court.getSportType(),
                court.getSurfaceType(),
                court.getCapacity(),
                court.getStatus(),
                court.getCreatedAt(),
                court.getVideoUrl(),
                scheduleDtos
        );
    }

    private CourtScheduleResponseDto toScheduleResponse(CourtSchedule schedule) {
        return new CourtScheduleResponseDto(
                schedule.getId(),
                schedule.getCourtId(),
                schedule.getDayOfWeek(),
                schedule.getOpenTime(),
                schedule.getCloseTime()
        );
    }

    public List<com.courthub.common.dto.analytics.CourtIssueInternalDTO> getAllCourtIssuesForAnalytics() {
        return courtIssueService.getAllCourtIssuesForAnalytics();
    }
}
