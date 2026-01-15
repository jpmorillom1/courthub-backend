package com.courthub.realtime.client;

import com.courthub.realtime.dto.AvailabilitySlotResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class BookingServiceClient {

    private final RestTemplate restTemplate;
    private final String bookingServiceBaseUrl;

    public BookingServiceClient(RestTemplate restTemplate, @Qualifier("bookingServiceBaseUrl") String bookingServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.bookingServiceBaseUrl = bookingServiceBaseUrl;
    }

    public List<AvailabilitySlotResponse> getSlotsByDate(LocalDate date) {
        try {
            String url = bookingServiceBaseUrl + "/bookings/internal/slots-sync?date=" + date;
            log.debug("Fetching slots from booking service: {}", url);
            
            ResponseEntity<List<AvailabilitySlotResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<AvailabilitySlotResponse>>() {}
            );
            
            List<AvailabilitySlotResponse> slots = response.getBody();
            log.info("Successfully retrieved {} slots for date {}", slots != null ? slots.size() : 0, date);
            return slots != null ? slots : Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch slots from booking service for date {}: {}", date, e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Unexpected error while fetching slots from booking service for date {}", date, e);
            return Collections.emptyList();
        }
    }
}
