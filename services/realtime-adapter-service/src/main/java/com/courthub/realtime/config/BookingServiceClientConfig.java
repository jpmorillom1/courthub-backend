package com.courthub.realtime.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BookingServiceClientConfig {

    @Value("${services.booking-url:http://localhost:8083}")
    private String bookingServiceUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public String bookingServiceBaseUrl() {
        return bookingServiceUrl;
    }
}
