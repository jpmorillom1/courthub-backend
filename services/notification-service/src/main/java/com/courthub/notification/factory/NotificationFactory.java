package com.courthub.notification.factory;


public interface NotificationFactory {

    MessageBody createMessageBody();


    DeliveryChannel createDeliveryChannel();
}
