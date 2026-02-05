package com.courthub.analytics.client;

import com.courthub.common.dto.analytics.CourtIssueInternalDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "COURT-SERVICE")
public interface CourtServiceFeignClient {

    @GetMapping("/courts/internal/courts/issues/all")
    List<CourtIssueInternalDTO> getAllCourtIssues();
}
