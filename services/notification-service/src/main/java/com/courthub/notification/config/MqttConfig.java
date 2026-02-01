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
    private String brokerUrl;

    @Value("${hivemq.client-id}")
    private String clientId;

    @Bean
    public MqttClient mqttClient() throws MqttException {
        log.info("Initializing MQTT client with broker: {}", brokerUrl);

        MqttClient client = new MqttClient(brokerUrl, clientId);
        MqttConnectOptions options = new MqttConnectOptions();

        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(60);

        try {
            client.connect(options);
            log.info("Successfully connected to Public HiveMQ broker");
        } catch (MqttException e) {
            log.error("Failed to connect to HiveMQ broker. Error: {}", e.getMessage());

            throw e;
        }

        return client;
    }
}