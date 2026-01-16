package com.courthub.notification.factory.impl;

import com.courthub.notification.factory.DeliveryChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;


@Slf4j
@RequiredArgsConstructor
public class EmailChannel implements DeliveryChannel {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    @Override
    public void send(String recipient, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(recipient);
            message.setSubject("CourtHub Notification");
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", recipient);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", recipient, e);
            throw new RuntimeException("Email delivery failed", e);
        }
    }
}
