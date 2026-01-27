package com.courthub.court.repository;

import com.courthub.court.domain.CourtIssue;
import com.courthub.court.domain.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourtIssueRepository extends JpaRepository<CourtIssue, UUID> {


    List<CourtIssue> findByCourtIdOrderByCreatedAtDesc(UUID courtId);


    @Query("SELECT ci FROM CourtIssue ci WHERE ci.status != :closedStatus ORDER BY ci.createdAt DESC")
    List<CourtIssue> findAllPendingIssues(@Param("closedStatus") IssueStatus closedStatus);


    @Query("SELECT ci FROM CourtIssue ci WHERE ci.courtId = :courtId AND ci.status != :closedStatus ORDER BY ci.createdAt DESC")
    List<CourtIssue> findPendingIssuesByCourtId(
            @Param("courtId") UUID courtId,
            @Param("closedStatus") IssueStatus closedStatus
    );


    @Query("SELECT COUNT(ci) FROM CourtIssue ci WHERE ci.status != :closedStatus")
    long countPendingIssues(@Param("closedStatus") IssueStatus closedStatus);
}
