package com.courthub.notification.factory;

public interface DeliveryChannel {

    void send(String recipient, String body);
}
