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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        return toResponseDto(savedIssue);
    }


    public List<CourtIssueResponseDto> getIssuesByCourtId(UUID courtId) {
        // Verify that the court exists
        if (!courtRepository.existsById(courtId)) {
            throw new NotFoundException("Court not found with id: " + courtId);
        }

        return issueRepository.findByCourtIdOrderByCreatedAtDesc(courtId).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<CourtIssueResponseDto> getAllPendingIssues() {
        return issueRepository.findAllPendingIssues(IssueStatus.CLOSED).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }


    public CourtIssueResponseDto getIssueById(UUID issueId) {
        CourtIssue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException("Issue not found with id: " + issueId));
        return toResponseDto(issue);
    }


    @Transactional
    public CourtIssueResponseDto updateIssueStatus(UUID issueId, IssueStatusUpdateDto statusUpdate) {
        CourtIssue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException("Issue not found with id: " + issueId));

        IssueStatus newStatus = statusUpdate.getStatus();

        // Validate status transition
        validateStatusTransition(issue.getStatus(), newStatus);

        issue.setStatus(newStatus);
        issue.setUpdatedAt(Instant.now());

        CourtIssue updatedIssue = issueRepository.save(issue);
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
}
