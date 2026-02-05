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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourtService Unit Tests")
public class CourtServiceTest {

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private CourtScheduleRepository scheduleRepository;

    @Mock
    private CourtEventProducer eventProducer;

    @InjectMocks
    private CourtService courtService;

    private Court testCourt;
    private CourtRequestDto courtRequestDto;
    private UUID courtId;

    @BeforeEach
    void setUp() {
        courtId = UUID.randomUUID();
        
        testCourt = new Court();
        testCourt.setId(courtId);
        testCourt.setName("Soccer Court A");
        testCourt.setLocation("Building 1");
        testCourt.setSportType(SportType.SOCCER);
        testCourt.setSurfaceType(SurfaceType.SYNTHETIC);
        testCourt.setCapacity(2);
        testCourt.setStatus(CourtStatus.ACTIVE);

        courtRequestDto = new CourtRequestDto();
        courtRequestDto.setName("Basketball Court");
        courtRequestDto.setLocation("Field 1");
        courtRequestDto.setSportType(SportType.BASKETBALL);
        courtRequestDto.setSurfaceType(SurfaceType.WOOD);
        courtRequestDto.setCapacity(10);
    }

    @Test
    @DisplayName("Should create court successfully")
    void testCreateCourtSuccess() {
        // Arrange
        when(courtRepository.save(any(Court.class))).thenReturn(testCourt);
        doNothing().when(eventProducer).sendCourtCreated(any(Court.class));

        // Act
        CourtResponseDto result = courtService.createCourt(courtRequestDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Soccer Court A");
        assertThat(result.getLocation()).isEqualTo("Building 1");
        assertThat(result.getSportType()).isEqualTo(SportType.SOCCER);
        assertThat(result.getSurfaceType()).isEqualTo(SurfaceType.SYNTHETIC);
        assertThat(result.getCapacity()).isEqualTo(2);
        assertThat(result.getStatus()).isEqualTo(CourtStatus.ACTIVE);

        verify(courtRepository, times(1)).save(any(Court.class));
        verify(eventProducer, times(1)).sendCourtCreated(any(Court.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when capacity is invalid")
    void testCreateCourtWithInvalidCapacity() {
        // Arrange
        courtRequestDto.setCapacity(0);

        // Act & Assert
        assertThatThrownBy(() -> courtService.createCourt(courtRequestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Capacity must be greater than zero");

        verify(courtRepository, never()).save(any(Court.class));
        verify(eventProducer, never()).sendCourtCreated(any(Court.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when capacity is negative")
    void testCreateCourtWithNegativeCapacity() {
        // Arrange
        courtRequestDto.setCapacity(-5);

        // Act & Assert
        assertThatThrownBy(() -> courtService.createCourt(courtRequestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Capacity must be greater than zero");

        verify(courtRepository, never()).save(any(Court.class));
    }

    @Test
    @DisplayName("Should list courts with filters")
    void testListCourtsWithFilters() {
        // Arrange
        List<Court> courts = List.of(testCourt);
        when(courtRepository.findByFilters(SportType.SOCCER, SurfaceType.SYNTHETIC, CourtStatus.ACTIVE))
                .thenReturn(courts);
        when(scheduleRepository.findByCourtIdIn(any())).thenReturn(Collections.emptyList());

        // Act
        List<CourtResponseDto> result = courtService.listCourts(SportType.SOCCER, SurfaceType.SYNTHETIC, CourtStatus.ACTIVE);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Soccer Court A");

        verify(courtRepository, times(1)).findByFilters(SportType.SOCCER, SurfaceType.SYNTHETIC, CourtStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should return empty list when no courts found")
    void testListCourtsEmpty() {
        // Arrange
        when(courtRepository.findByFilters(any(), any(), any())).thenReturn(Collections.emptyList());

        // Act
        List<CourtResponseDto> result = courtService.listCourts(SportType.SOCCER, SurfaceType.SYNTHETIC, CourtStatus.ACTIVE);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(scheduleRepository, never()).findByCourtIdIn(any());
    }

    @Test
    @DisplayName("Should get court by ID successfully")
    void testGetCourtSuccess() {
        // Arrange
        when(courtRepository.findById(courtId)).thenReturn(Optional.of(testCourt));
        when(scheduleRepository.findByCourtId(courtId)).thenReturn(Collections.emptyList());

        // Act
        CourtResponseDto result = courtService.getCourt(courtId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(courtId);
        assertThat(result.getName()).isEqualTo("Soccer Court A");

        verify(courtRepository, times(1)).findById(courtId);
        verify(scheduleRepository, times(1)).findByCourtId(courtId);
    }

    @Test
    @DisplayName("Should throw NotFoundException when court not found")
    void testGetCourtNotFound() {
        // Arrange
        when(courtRepository.findById(courtId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> courtService.getCourt(courtId))
                .isInstanceOf(NotFoundException.class);

        verify(courtRepository, times(1)).findById(courtId);
        verify(scheduleRepository, never()).findByCourtId(courtId);
    }

    @Test
    @DisplayName("Should update court status successfully")
    void testUpdateStatusSuccess() {
        // Arrange
        CourtStatusUpdateDto statusUpdate = new CourtStatusUpdateDto();
        statusUpdate.setStatus(CourtStatus.MAINTENANCE);

        Court updatedCourt = new Court();
        updatedCourt.setId(courtId);
        updatedCourt.setName("Soccer Court A");
        updatedCourt.setLocation("Building 1");
        updatedCourt.setSportType(SportType.SOCCER);
        updatedCourt.setSurfaceType(SurfaceType.SYNTHETIC);
        updatedCourt.setCapacity(2);
        updatedCourt.setStatus(CourtStatus.MAINTENANCE);

        when(courtRepository.findById(courtId)).thenReturn(Optional.of(testCourt));
        when(courtRepository.save(any(Court.class))).thenReturn(updatedCourt);
        when(scheduleRepository.findByCourtId(courtId)).thenReturn(Collections.emptyList());
        doNothing().when(eventProducer).sendCourtStatusChanged(any(Court.class));
        doNothing().when(eventProducer).sendCourtUpdated(any(Court.class));

        // Act
        CourtResponseDto result = courtService.updateStatus(courtId, statusUpdate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(CourtStatus.MAINTENANCE);

        verify(courtRepository, times(1)).findById(courtId);
        verify(courtRepository, times(1)).save(any(Court.class));
        verify(eventProducer, times(1)).sendCourtStatusChanged(any(Court.class));
        verify(eventProducer, times(1)).sendCourtUpdated(any(Court.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when status is null")
    void testUpdateStatusWithNullStatus() {
        // Arrange
        CourtStatusUpdateDto statusUpdate = new CourtStatusUpdateDto();
        statusUpdate.setStatus(null);

        // Act & Assert
        assertThatThrownBy(() -> courtService.updateStatus(courtId, statusUpdate))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Status is required");
    }

    @Test
    @DisplayName("Should throw NotFoundException when updating status for non-existent court")
    void testUpdateStatusCourtNotFound() {
        // Arrange
        CourtStatusUpdateDto statusUpdate = new CourtStatusUpdateDto();
        statusUpdate.setStatus(CourtStatus.ACTIVE);

        when(courtRepository.findById(courtId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> courtService.updateStatus(courtId, statusUpdate))
                .isInstanceOf(NotFoundException.class);

        verify(courtRepository, never()).save(any(Court.class));
    }

    @Test
    @DisplayName("Should upsert schedule successfully")
    void testUpsertScheduleSuccess() {
        // Arrange
        CourtScheduleRequestDto scheduleRequest = new CourtScheduleRequestDto();
        scheduleRequest.setDayOfWeek(1); // Monday = 1
        scheduleRequest.setOpenTime(LocalTime.of(9, 0));
        scheduleRequest.setCloseTime(LocalTime.of(17, 0));

        CourtSchedule schedule = new CourtSchedule();
        schedule.setCourtId(courtId);
        schedule.setDayOfWeek(1); // Monday = 1
        schedule.setOpenTime(LocalTime.of(9, 0));
        schedule.setCloseTime(LocalTime.of(17, 0));

        when(courtRepository.findById(courtId)).thenReturn(Optional.of(testCourt));
        when(scheduleRepository.findByCourtIdAndDayOfWeek(courtId, 1))
                .thenReturn(Optional.empty());
        when(scheduleRepository.save(any(CourtSchedule.class))).thenReturn(schedule);
        doNothing().when(eventProducer).sendCourtScheduleUpdated(any(Court.class), any(CourtSchedule.class));
        doNothing().when(eventProducer).sendCourtUpdated(any(Court.class));

        // Act
        CourtScheduleResponseDto result = courtService.upsertSchedule(courtId, scheduleRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDayOfWeek()).isEqualTo(1);
        assertThat(result.getOpenTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(result.getCloseTime()).isEqualTo(LocalTime.of(17, 0));

        verify(courtRepository, times(1)).findById(courtId);
        verify(scheduleRepository, times(1)).save(any(CourtSchedule.class));
        verify(eventProducer, times(1)).sendCourtScheduleUpdated(any(Court.class), any(CourtSchedule.class));
        verify(eventProducer, times(1)).sendCourtUpdated(any(Court.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when open time is null")
    void testUpsertScheduleWithNullOpenTime() {
        // Arrange
        CourtScheduleRequestDto scheduleRequest = new CourtScheduleRequestDto();
        scheduleRequest.setDayOfWeek(1); // Monday = 1
        scheduleRequest.setOpenTime(null);
        scheduleRequest.setCloseTime(LocalTime.of(17, 0));

        when(courtRepository.findById(courtId)).thenReturn(Optional.of(testCourt));

        // Act & Assert
        assertThatThrownBy(() -> courtService.upsertSchedule(courtId, scheduleRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Open and close time are required");

        verify(scheduleRepository, never()).save(any(CourtSchedule.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when close time is null")
    void testUpsertScheduleWithNullCloseTime() {
        // Arrange
        CourtScheduleRequestDto scheduleRequest = new CourtScheduleRequestDto();
        scheduleRequest.setDayOfWeek(1); // Monday = 1
        scheduleRequest.setOpenTime(LocalTime.of(9, 0));
        scheduleRequest.setCloseTime(null);

        when(courtRepository.findById(courtId)).thenReturn(Optional.of(testCourt));

        // Act & Assert
        assertThatThrownBy(() -> courtService.upsertSchedule(courtId, scheduleRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Open and close time are required");

        verify(scheduleRepository, never()).save(any(CourtSchedule.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when open time is after close time")
    void testUpsertScheduleWithInvalidTimes() {
        // Arrange
        CourtScheduleRequestDto scheduleRequest = new CourtScheduleRequestDto();
        scheduleRequest.setDayOfWeek(1); // Monday = 1
        scheduleRequest.setOpenTime(LocalTime.of(17, 0));
        scheduleRequest.setCloseTime(LocalTime.of(9, 0));

        when(courtRepository.findById(courtId)).thenReturn(Optional.of(testCourt));

        // Act & Assert
        assertThatThrownBy(() -> courtService.upsertSchedule(courtId, scheduleRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Open time must be before close time");

        verify(scheduleRepository, never()).save(any(CourtSchedule.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when open time equals close time")
    void testUpsertScheduleWithEqualTimes() {
        // Arrange
        CourtScheduleRequestDto scheduleRequest = new CourtScheduleRequestDto();
        scheduleRequest.setDayOfWeek(1); // Monday = 1
        scheduleRequest.setOpenTime(LocalTime.of(9, 0));
        scheduleRequest.setCloseTime(LocalTime.of(9, 0));

        when(courtRepository.findById(courtId)).thenReturn(Optional.of(testCourt));

        // Act & Assert
        assertThatThrownBy(() -> courtService.upsertSchedule(courtId, scheduleRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Open time must be before close time");

        verify(scheduleRepository, never()).save(any(CourtSchedule.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when upserting schedule for non-existent court")
    void testUpsertScheduleCourtNotFound() {
        // Arrange
        CourtScheduleRequestDto scheduleRequest = new CourtScheduleRequestDto();
        scheduleRequest.setDayOfWeek(1); // Monday = 1
        scheduleRequest.setOpenTime(LocalTime.of(9, 0));
        scheduleRequest.setCloseTime(LocalTime.of(17, 0));

        when(courtRepository.findById(courtId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> courtService.upsertSchedule(courtId, scheduleRequest))
                .isInstanceOf(NotFoundException.class);

        verify(scheduleRepository, never()).save(any(CourtSchedule.class));
    }
}
