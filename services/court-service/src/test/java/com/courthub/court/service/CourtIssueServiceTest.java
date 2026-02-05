package com.courthub.court.service;

import com.courthub.common.exception.BusinessException;
import com.courthub.common.exception.NotFoundException;
import com.courthub.court.domain.CourtIssue;
import com.courthub.court.domain.IssueSeverity;
import com.courthub.court.domain.IssueStatus;
import com.courthub.court.dto.CourtIssueRequestDto;
import com.courthub.court.dto.CourtIssueResponseDto;
import com.courthub.court.dto.IssueStatusUpdateDto;
import com.courthub.court.repository.CourtIssueRepository;
import com.courthub.court.repository.CourtRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourtIssueService Unit Tests")
public class CourtIssueServiceTest {

    @Mock
    private CourtIssueRepository issueRepository;

    @Mock
    private CourtRepository courtRepository;

    @InjectMocks
    private CourtIssueService issueService;

    private UUID courtId;
    private UUID reporterId;
    private UUID issueId;
    private CourtIssue testIssue;
    private CourtIssueRequestDto issueRequestDto;

    @BeforeEach
    void setUp() {
        courtId = UUID.randomUUID();
        reporterId = UUID.randomUUID();
        issueId = UUID.randomUUID();

        testIssue = new CourtIssue();
        testIssue.setId(issueId);
        testIssue.setCourtId(courtId);
        testIssue.setReporterId(reporterId);
        testIssue.setTitle("Broken court light");
        testIssue.setDescription("The main light in court 1 is not working");
        testIssue.setSeverity(IssueSeverity.MEDIUM);
        testIssue.setStatus(IssueStatus.REPORTED);
        testIssue.setCreatedAt(Instant.now());
        testIssue.setUpdatedAt(Instant.now());

        issueRequestDto = new CourtIssueRequestDto();
        issueRequestDto.setCourtId(courtId);
        issueRequestDto.setTitle("Broken court light");
        issueRequestDto.setDescription("The main light in court 1 is not working");
        issueRequestDto.setSeverity(IssueSeverity.MEDIUM);
    }

    @Test
    @DisplayName("Should create issue successfully")
    void testCreateIssueSuccess() {
        // Arrange
        when(courtRepository.existsById(courtId)).thenReturn(true);
        when(issueRepository.save(any(CourtIssue.class))).thenReturn(testIssue);

        // Act
        CourtIssueResponseDto result = issueService.createIssue(issueRequestDto, reporterId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(issueId);
        assertThat(result.getCourtId()).isEqualTo(courtId);
        assertThat(result.getReporterId()).isEqualTo(reporterId);
        assertThat(result.getStatus()).isEqualTo(IssueStatus.REPORTED);

        verify(courtRepository, times(1)).existsById(courtId);
        verify(issueRepository, times(1)).save(any(CourtIssue.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when court not found")
    void testCreateIssueCourtNotFound() {
        // Arrange
        when(courtRepository.existsById(courtId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> issueService.createIssue(issueRequestDto, reporterId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Court not found");

        verify(courtRepository, times(1)).existsById(courtId);
        verify(issueRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get issues by court ID")
    void testGetIssuesByCourtId() {
        // Arrange
        List<CourtIssue> issues = Collections.singletonList(testIssue);
        when(courtRepository.existsById(courtId)).thenReturn(true);
        when(issueRepository.findByCourtIdOrderByCreatedAtDesc(courtId)).thenReturn(issues);

        // Act
        List<CourtIssueResponseDto> result = issueService.getIssuesByCourtId(courtId);

        // Assert
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(issueId);

        verify(courtRepository, times(1)).existsById(courtId);
        verify(issueRepository, times(1)).findByCourtIdOrderByCreatedAtDesc(courtId);
    }

    @Test
    @DisplayName("Should get all pending issues")
    void testGetAllPendingIssues() {
        // Arrange
        List<CourtIssue> issues = Collections.singletonList(testIssue);
        when(issueRepository.findAllPendingIssues(IssueStatus.CLOSED)).thenReturn(issues);

        // Act
        List<CourtIssueResponseDto> result = issueService.getAllPendingIssues();

        // Assert
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(IssueStatus.REPORTED);

        verify(issueRepository, times(1)).findAllPendingIssues(IssueStatus.CLOSED);
    }

    @Test
    @DisplayName("Should get issue by ID")
    void testGetIssueById() {
        // Arrange
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));

        // Act
        CourtIssueResponseDto result = issueService.getIssueById(issueId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(issueId);

        verify(issueRepository, times(1)).findById(issueId);
    }

    @Test
    @DisplayName("Should throw NotFoundException when issue not found by ID")
    void testGetIssueByIdNotFound() {
        // Arrange
        when(issueRepository.findById(issueId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> issueService.getIssueById(issueId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Issue not found");

        verify(issueRepository, times(1)).findById(issueId);
    }

    @Test
    @DisplayName("Should update issue status successfully")
    void testUpdateIssueStatusSuccess() {
        // Arrange
        CourtIssue updatedIssue = new CourtIssue();
        updatedIssue.setId(issueId);
        updatedIssue.setCourtId(courtId);
        updatedIssue.setReporterId(reporterId);
        updatedIssue.setTitle(testIssue.getTitle());
        updatedIssue.setDescription(testIssue.getDescription());
        updatedIssue.setSeverity(testIssue.getSeverity());
        updatedIssue.setStatus(IssueStatus.IN_PROGRESS);
        updatedIssue.setCreatedAt(testIssue.getCreatedAt());
        updatedIssue.setUpdatedAt(Instant.now());

        IssueStatusUpdateDto statusUpdate = new IssueStatusUpdateDto(IssueStatus.IN_PROGRESS);
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));
        when(issueRepository.save(any(CourtIssue.class))).thenReturn(updatedIssue);

        // Act
        CourtIssueResponseDto result = issueService.updateIssueStatus(issueId, statusUpdate);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(IssueStatus.IN_PROGRESS);

        verify(issueRepository, times(1)).findById(issueId);
        verify(issueRepository, times(1)).save(any(CourtIssue.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when updating closed issue")
    void testUpdateClosedIssueThrowsException() {
        // Arrange
        CourtIssue closedIssue = new CourtIssue();
        closedIssue.setId(issueId);
        closedIssue.setStatus(IssueStatus.CLOSED);

        IssueStatusUpdateDto statusUpdate = new IssueStatusUpdateDto(IssueStatus.RESOLVED);
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(closedIssue));

        // Act & Assert
        assertThatThrownBy(() -> issueService.updateIssueStatus(issueId, statusUpdate))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot change status of a closed issue");

        verify(issueRepository, times(1)).findById(issueId);
        verify(issueRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when setting same status")
    void testUpdateSameStatusThrowsException() {
        // Arrange
        IssueStatusUpdateDto statusUpdate = new IssueStatusUpdateDto(IssueStatus.REPORTED);
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(testIssue));

        // Act & Assert
        assertThatThrownBy(() -> issueService.updateIssueStatus(issueId, statusUpdate))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Issue already has status");

        verify(issueRepository, times(1)).findById(issueId);
        verify(issueRepository, never()).save(any());
    }
}
