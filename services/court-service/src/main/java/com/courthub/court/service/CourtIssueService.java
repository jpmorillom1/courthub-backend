package com.courthub.court.service;

import com.courthub.common.exception.BusinessException;
import com.courthub.common.exception.NotFoundException;
import com.courthub.court.domain.CourtIssue;
import com.courthub.court.domain.IssueStatus;
import com.courthub.court.dto.CourtIssueRequestDto;
import com.courthub.court.dto.CourtIssueResponseDto;
import com.courthub.court.dto.IssueStatusUpdateDto;
import com.courthub.court.repository.CourtIssueRepository;
import com.courthub.court.repository.CourtRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CourtIssueService {

    private final CourtIssueRepository issueRepository;
    private final CourtRepository courtRepository;

    public CourtIssueService(CourtIssueRepository issueRepository, CourtRepository courtRepository) {
        this.issueRepository = issueRepository;
        this.courtRepository = courtRepository;
    }


    @Transactional
    public CourtIssueResponseDto createIssue(CourtIssueRequestDto request, UUID reporterId) {
        log.info("Creating court issue: courtId={}, reporterId={}, severity={}",
                request.getCourtId(), reporterId, request.getSeverity());
        // Verify that the court exists
        if (!courtRepository.existsById(request.getCourtId())) {
            throw new NotFoundException("Court not found with id: " + request.getCourtId());
        }

        CourtIssue issue = new CourtIssue();
        issue.setCourtId(request.getCourtId());
        issue.setReporterId(reporterId);
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setSeverity(request.getSeverity());
        // Status will be set to REPORTED by @PrePersist

        CourtIssue savedIssue = issueRepository.save(issue);
        log.info("Court issue created successfully: issueId={}", savedIssue.getId());
        return toResponseDto(savedIssue);
    }


    public List<CourtIssueResponseDto> getIssuesByCourtId(UUID courtId) {
        log.debug("Fetching issues for courtId={}", courtId);
        // Verify that the court exists
        if (!courtRepository.existsById(courtId)) {
            throw new NotFoundException("Court not found with id: " + courtId);
        }

        return issueRepository.findByCourtIdOrderByCreatedAtDesc(courtId).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<CourtIssueResponseDto> getAllPendingIssues() {
        log.debug("Fetching all pending issues");
        return issueRepository.findAllPendingIssues(IssueStatus.CLOSED).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }


    public CourtIssueResponseDto getIssueById(UUID issueId) {
        log.debug("Fetching issue by id={}", issueId);
        CourtIssue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException("Issue not found with id: " + issueId));
        return toResponseDto(issue);
    }


    @Transactional
    public CourtIssueResponseDto updateIssueStatus(UUID issueId, IssueStatusUpdateDto statusUpdate) {
        log.info("Updating issue status: issueId={}, status={}", issueId, statusUpdate.getStatus());
        CourtIssue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException("Issue not found with id: " + issueId));

        IssueStatus newStatus = statusUpdate.getStatus();

        // Validate status transition
        validateStatusTransition(issue.getStatus(), newStatus);

        issue.setStatus(newStatus);
        issue.setUpdatedAt(Instant.now());

        CourtIssue updatedIssue = issueRepository.save(issue);
        log.info("Issue status updated successfully: issueId={}, status={}", issueId, updatedIssue.getStatus());
        return toResponseDto(updatedIssue);
    }


    private void validateStatusTransition(IssueStatus currentStatus, IssueStatus newStatus) {
        if (currentStatus == IssueStatus.CLOSED) {
            throw new BusinessException("Cannot change status of a closed issue");
        }

        if (currentStatus == newStatus) {
            throw new BusinessException("Issue already has status: " + newStatus);
        }
    }


    private CourtIssueResponseDto toResponseDto(CourtIssue issue) {
        return new CourtIssueResponseDto(
                issue.getId(),
                issue.getCourtId(),
                issue.getReporterId(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getSeverity(),
                issue.getStatus(),
                issue.getCreatedAt(),
                issue.getUpdatedAt()
        );
    }

    public List<com.courthub.common.dto.analytics.CourtIssueInternalDTO> getAllCourtIssuesForAnalytics() {
        log.debug("Fetching court issues for analytics");
        return issueRepository.findAll().stream()
            .map(issue -> new com.courthub.common.dto.analytics.CourtIssueInternalDTO(
                issue.getId().toString(),
                issue.getCourtId().toString(),
                issue.getSeverity().toString(),
                issue.getStatus().toString(),
                toLocalDateTime(issue.getCreatedAt()),
                toLocalDateTime(issue.getUpdatedAt())
            ))
            .collect(Collectors.toList());
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
