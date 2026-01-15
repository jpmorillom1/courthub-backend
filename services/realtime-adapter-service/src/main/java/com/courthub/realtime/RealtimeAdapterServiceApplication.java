package com.courthub.realtime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class RealtimeAdapterServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RealtimeAdapterServiceApplication.class, args);
    }
}
