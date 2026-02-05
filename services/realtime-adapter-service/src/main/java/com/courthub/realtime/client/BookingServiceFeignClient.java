package com.courthub.realtime.client;

import com.courthub.realtime.dto.AvailabilitySlotResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "BOOKING-SERVICE")
public interface BookingServiceFeignClient {

    @GetMapping("/bookings/internal/slots-sync")
    List<AvailabilitySlotResponse> getSlotsByDate(@RequestParam("date") LocalDate date);
}
