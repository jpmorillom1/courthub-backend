package com.courthub.notification.factory.impl;

import com.courthub.notification.factory.DeliveryChannel;
import com.courthub.notification.factory.MessageBody;
import com.courthub.notification.factory.NotificationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;


@Component("formalNotificationFactory")
@RequiredArgsConstructor
public class FormalNotificationFactory implements NotificationFactory {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public MessageBody createMessageBody() {
        return new FormalMessageBody();
    }

    @Override
    public DeliveryChannel createDeliveryChannel() {
        return new EmailChannel(mailSender, fromAddress);
    }
}