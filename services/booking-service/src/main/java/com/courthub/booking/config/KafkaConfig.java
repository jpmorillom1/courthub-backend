package com.courthub.booking.config;

import com.courthub.booking.event.CourtEventPayload;
import com.courthub.booking.event.CourtScheduleEventPayload;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CourtEventPayload> courtEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CourtEventPayload> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(courtEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CourtScheduleEventPayload> courtScheduleKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CourtScheduleEventPayload> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(courtScheduleConsumerFactory());
        return factory;
    }

    private DefaultKafkaConsumerFactory<String, CourtEventPayload> courtEventConsumerFactory() {
        JsonDeserializer<CourtEventPayload> deserializer = new JsonDeserializer<>(CourtEventPayload.class);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(false);

        Map<String, Object> props = baseConsumerProps();

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    private DefaultKafkaConsumerFactory<String, CourtScheduleEventPayload> courtScheduleConsumerFactory() {
        JsonDeserializer<CourtScheduleEventPayload> deserializer = new JsonDeserializer<>(CourtScheduleEventPayload.class);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(false);

        Map<String, Object> props = baseConsumerProps();

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    private Map<String, Object> baseConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "booking-service");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }
}
