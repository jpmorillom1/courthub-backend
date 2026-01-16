package com.courthub.notification.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
public class MqttConfig {

    @Value("${hivemq.broker-url}")
    private String brokerUrl; // Ahora será tcp://broker.hivemq.com:1883

    @Value("${hivemq.client-id}")
    private String clientId;

    @Bean
    public MqttClient mqttClient() throws MqttException {
        log.info("Initializing MQTT client with broker: {}", brokerUrl);

        MqttClient client = new MqttClient(brokerUrl, clientId);
        MqttConnectOptions options = new MqttConnectOptions();

        // Al ser un broker público gratuito, omitimos setUserName y setPassword
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(60);

        try {
            client.connect(options);
            log.info("Successfully connected to Public HiveMQ broker");
        } catch (MqttException e) {
            log.error("Failed to connect to HiveMQ broker. Error: {}", e.getMessage());
            // Opcional: No lanzar la excepción si quieres que el microservicio
            // arranque aunque el MQTT falle (resiliencia).
            throw e;
        }

        return client;
    }
}