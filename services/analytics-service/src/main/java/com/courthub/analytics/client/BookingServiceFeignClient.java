package com.courthub.analytics.client;

import com.courthub.common.dto.analytics.BookingInternalDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "BOOKING-SERVICE")
public interface BookingServiceFeignClient {

    @GetMapping("/bookings/internal/bookings/all")
    List<BookingInternalDTO> getAllBookings();
}
