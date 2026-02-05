package com.courthub.notification.factory.impl;

import com.courthub.notification.factory.DeliveryChannel;
import com.courthub.notification.factory.MessageBody;
import com.courthub.notification.factory.NotificationFactory;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component("informalNotificationFactory")
@RequiredArgsConstructor
public class InformalNotificationFactory implements NotificationFactory {

    private final MqttClient mqttClient;

    @Value("${hivemq.topic}")
    private String topic;

    @Value("${hivemq.qos}")
    private int qos;

    @Override
    public MessageBody createMessageBody() {
        return new InformalMessageBody();
    }

    @Override
    public DeliveryChannel createDeliveryChannel() {
        return new MqttChannel(mqttClient, topic, qos);
    }
}
