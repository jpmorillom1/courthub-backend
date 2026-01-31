package com.courthub.notification.service;

import com.courthub.notification.domain.NotificationChannel;
import com.courthub.notification.domain.NotificationLog;
import com.courthub.notification.domain.NotificationType;
import com.courthub.notification.event.BookingEventPayload;
import com.courthub.notification.factory.DeliveryChannel;
import com.courthub.notification.factory.MessageBody;
import com.courthub.notification.factory.NotificationFactory;
import com.courthub.notification.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service for handling notification operations.
 * Orchestrates the Abstract Factory pattern and persistence.
 */
@Slf4j
@Service
public class NotificationService {

    @Qualifier("formalNotificationFactory")
    private final NotificationFactory formalFactory;

    @Qualifier("informalNotificationFactory")
    private final NotificationFactory informalFactory;

    private final NotificationRepository notificationRepository;
    private final UserServiceClient userServiceClient;
    
    public NotificationService(
            @Qualifier("formalNotificationFactory") NotificationFactory formalFactory,
            @Qualifier("informalNotificationFactory") NotificationFactory informalFactory,
            NotificationRepository notificationRepository,
            UserServiceClient userServiceClient) {
        this.formalFactory = formalFactory;
        this.informalFactory = informalFactory;
        this.notificationRepository = notificationRepository;
        this.userServiceClient = userServiceClient;
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");


    public void sendBookingConfirmation(BookingEventPayload event) {
        log.info("Sending booking confirmation for booking ID: {}", event.getBookingId());

        String userEmail = userServiceClient.getUserEmail(event.getUserId());

        MessageBody messageBody = formalFactory.createMessageBody();
        DeliveryChannel deliveryChannel = formalFactory.createDeliveryChannel();

        String bookingData = formatBookingData(event);
        String formattedMessage = messageBody.format(bookingData);

        NotificationLog log = NotificationLog.builder()
                .userId(event.getUserId())
                .type(NotificationType.FORMAL)
                .channel(NotificationChannel.EMAIL)
                .message(formattedMessage)
                .recipient(userEmail)
                .subject("Booking Confirmation")
                .timestamp(LocalDateTime.now())
                .build();

        try {
            deliveryChannel.send(userEmail, formattedMessage);
            log.setSuccess(true);
        } catch (Exception e) {
            log.setSuccess(false);
            log.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            notificationRepository.save(log);
        }
    }


    public void sendInformalNotification(UUID userId, String message) {
        log.info("Sending informal notification for user ID: {}", userId);

        MessageBody messageBody = informalFactory.createMessageBody();
        DeliveryChannel deliveryChannel = informalFactory.createDeliveryChannel();

        String formattedMessage = messageBody.format(message);

        NotificationLog log = NotificationLog.builder()
                .userId(userId)
                .type(NotificationType.INFORMAL)
                .channel(NotificationChannel.MQTT)
                .message(formattedMessage)
                .recipient(userId.toString())
                .timestamp(LocalDateTime.now())
                .build();

        try {
            deliveryChannel.send(userId.toString(), formattedMessage);
            log.setSuccess(true);
        } catch (Exception e) {
            log.setSuccess(false);
            log.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            notificationRepository.save(log);
        }
    }

    private String formatBookingData(BookingEventPayload event) {
        DateTimeFormatter dateService = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        DateTimeFormatter timeService = DateTimeFormatter.ofPattern("hh:mm a");

        return String.format(
                "Your booking has been confirmed!\n\n" +
                        "Booking ID: %s\n" +
                        "Date: %s\n" +
                        "Time Slot: %s - %s\n" +
                        "Status: %s",
                event.getBookingId(),
                event.getDate().format(dateService),
                event.getStartTime().format(timeService),
                event.getEndTime().format(timeService),
                event.getStatus()
        );
    }
}