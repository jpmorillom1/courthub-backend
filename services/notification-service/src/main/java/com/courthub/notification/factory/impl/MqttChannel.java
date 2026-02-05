package com.courthub.notification.factory.impl;

import com.courthub.notification.factory.DeliveryChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;


@Slf4j
@RequiredArgsConstructor
public class MqttChannel implements DeliveryChannel {

    private final MqttClient mqttClient;
    private final String topic;
    private final int qos;

    @Override
    public void send(String recipient, String body) {
        try {
            if (!mqttClient.isConnected()) {
                log.warn("MQTT client not connected. Attempting reconnection...");
                mqttClient.reconnect();
            }

            MqttMessage message = new MqttMessage(body.getBytes());
            message.setQos(qos);
            message.setRetained(false);

            String publishTopic = topic + "/" + recipient;
            mqttClient.publish(publishTopic, message);
            
            log.info("MQTT message published to topic: {}", publishTopic);
        } catch (Exception e) {
            log.error("Failed to publish MQTT message to topic: {}/{}", topic, recipient, e);
            throw new RuntimeException("MQTT delivery failed", e);
        }
    }
}
