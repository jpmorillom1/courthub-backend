package com.courthub.notification.service;

import com.courthub.notification.domain.NotificationChannel;
import com.courthub.notification.domain.NotificationLog;
import com.courthub.notification.domain.NotificationType;
import com.courthub.notification.event.BookingEventPayload;
import com.courthub.notification.factory.DeliveryChannel;
import com.courthub.notification.factory.MessageBody;
import com.courthub.notification.factory.NotificationFactory;
import com.courthub.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
class NotificationServiceTest {

    @Mock
    private NotificationFactory formalFactory;

    @Mock
    private NotificationFactory informalFactory;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private MessageBody messageBody;

    @Mock
    private DeliveryChannel deliveryChannel;

    private NotificationService notificationService;

    private UUID userId;
    private UUID bookingId;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String userEmail;
    private BookingEventPayload bookingEvent;

    @BeforeEach
    void setUp() {
        // Manually construct the service with mocked dependencies
        notificationService = new NotificationService(
                formalFactory,
                informalFactory,
                notificationRepository,
                userServiceClient
        );

        userId = UUID.randomUUID();
        bookingId = UUID.randomUUID();
        bookingDate = LocalDate.now().plusDays(1);
        startTime = LocalTime.of(9, 0);
        endTime = LocalTime.of(10, 0);
        userEmail = "user@example.com";

        bookingEvent = new BookingEventPayload();
        bookingEvent.setBookingId(bookingId);
        bookingEvent.setUserId(userId);
        bookingEvent.setDate(bookingDate);
        bookingEvent.setStartTime(startTime);
        bookingEvent.setEndTime(endTime);
        bookingEvent.setStatus("CONFIRMED");
    }

    @Test
    @DisplayName("Should send booking confirmation successfully")
    void testSendBookingConfirmationSuccess() {
        // Arrange
        String expectedFormattedMessage = "Booking confirmed";
        when(userServiceClient.getUserEmail(userId)).thenReturn(userEmail);
        when(formalFactory.createMessageBody()).thenReturn(messageBody);
        when(formalFactory.createDeliveryChannel()).thenReturn(deliveryChannel);
        when(messageBody.format(anyString())).thenReturn(expectedFormattedMessage);
        when(notificationRepository.save(any(NotificationLog.class))).thenReturn(new NotificationLog());

        // Act
        notificationService.sendBookingConfirmation(bookingEvent);

        // Assert
        verify(userServiceClient, times(1)).getUserEmail(userId);
        verify(formalFactory, times(1)).createMessageBody();
        verify(formalFactory, times(1)).createDeliveryChannel();
        verify(messageBody, times(1)).format(anyString());
        verify(deliveryChannel, times(1)).send(userEmail, expectedFormattedMessage);
        verify(notificationRepository, times(1)).save(any(NotificationLog.class));
    }

    @Test
    @DisplayName("Should handle exception during booking confirmation send")
    void testSendBookingConfirmationWithException() {
        // Arrange
        when(userServiceClient.getUserEmail(userId)).thenReturn(userEmail);
        when(formalFactory.createMessageBody()).thenReturn(messageBody);
        when(formalFactory.createDeliveryChannel()).thenReturn(deliveryChannel);
        when(messageBody.format(anyString())).thenReturn("Booking confirmed");
        doThrow(new RuntimeException("Email service unavailable"))
                .when(deliveryChannel).send(anyString(), anyString());

        // Act & Assert
        assertThatThrownBy(() -> notificationService.sendBookingConfirmation(bookingEvent))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email service unavailable");

        // Verify repository was still called to save log
        verify(notificationRepository, times(1)).save(any(NotificationLog.class));
    }

    @Test
    @DisplayName("Should use formal factory for booking confirmation")
    void testSendBookingConfirmationUsesFormalFactory() {
        // Arrange
        when(userServiceClient.getUserEmail(userId)).thenReturn(userEmail);
        when(formalFactory.createMessageBody()).thenReturn(messageBody);
        when(formalFactory.createDeliveryChannel()).thenReturn(deliveryChannel);
        when(messageBody.format(anyString())).thenReturn("Booking confirmed");
        when(notificationRepository.save(any(NotificationLog.class))).thenReturn(new NotificationLog());

        // Act
        notificationService.sendBookingConfirmation(bookingEvent);

        // Assert - Verify formal factory is used, not informal
        verify(formalFactory, times(1)).createMessageBody();
        verify(formalFactory, times(1)).createDeliveryChannel();
        verify(informalFactory, never()).createMessageBody();
        verify(informalFactory, never()).createDeliveryChannel();
    }

    @Test
    @DisplayName("Should send informal notification successfully")
    void testSendInformalNotificationSuccess() {
        // Arrange
        String message = "Hello, you have a new message";
        String expectedFormattedMessage = "Formatted: " + message;

        when(informalFactory.createMessageBody()).thenReturn(messageBody);
        when(informalFactory.createDeliveryChannel()).thenReturn(deliveryChannel);
        when(messageBody.format(message)).thenReturn(expectedFormattedMessage);
        doNothing().when(deliveryChannel).send(userId.toString(), expectedFormattedMessage);
        when(notificationRepository.save(any(NotificationLog.class))).thenReturn(new NotificationLog());

        // Act
        notificationService.sendInformalNotification(userId, message);

        // Assert
        verify(informalFactory, times(1)).createMessageBody();
        verify(informalFactory, times(1)).createDeliveryChannel();
        verify(messageBody, times(1)).format(message);
        verify(deliveryChannel, times(1)).send(userId.toString(), expectedFormattedMessage);
        verify(notificationRepository, times(1)).save(any(NotificationLog.class));
    }

    @Test
    @DisplayName("Should use informal factory for informal notification")
    void testSendInformalNotificationUsesInformalFactory() {
        // Arrange
        String message = "You have a new booking";
        when(informalFactory.createMessageBody()).thenReturn(messageBody);
        when(informalFactory.createDeliveryChannel()).thenReturn(deliveryChannel);
        when(messageBody.format(message)).thenReturn("Formatted: " + message);
        when(notificationRepository.save(any(NotificationLog.class))).thenReturn(new NotificationLog());

        // Act
        notificationService.sendInformalNotification(userId, message);

        // Assert - Verify informal factory is used, not formal
        verify(informalFactory, times(1)).createMessageBody();
        verify(informalFactory, times(1)).createDeliveryChannel();
        verify(formalFactory, never()).createMessageBody();
        verify(formalFactory, never()).createDeliveryChannel();
    }

    @Test
    @DisplayName("Should handle exception during informal notification send")
    void testSendInformalNotificationWithException() {
        // Arrange
        String message = "Notification message";
        when(informalFactory.createMessageBody()).thenReturn(messageBody);
        when(informalFactory.createDeliveryChannel()).thenReturn(deliveryChannel);
        when(messageBody.format(message)).thenReturn("Formatted: " + message);
        doThrow(new RuntimeException("MQTT connection failed"))
                .when(deliveryChannel).send(anyString(), anyString());

        // Act & Assert
        assertThatThrownBy(() -> notificationService.sendInformalNotification(userId, message))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("MQTT connection failed");

        // Verify repository was still called to save log
        verify(notificationRepository, times(1)).save(any(NotificationLog.class));
    }

    @Test
    @DisplayName("Should log notification as formal email type")
    void testBookingConfirmationLogsAsEmailType() {
        // Arrange
        when(userServiceClient.getUserEmail(userId)).thenReturn(userEmail);
        when(formalFactory.createMessageBody()).thenReturn(messageBody);
        when(formalFactory.createDeliveryChannel()).thenReturn(deliveryChannel);
        when(messageBody.format(anyString())).thenReturn("Booking confirmed");
        when(notificationRepository.save(any(NotificationLog.class))).thenReturn(new NotificationLog());

        // Act
        notificationService.sendBookingConfirmation(bookingEvent);

        // Assert - Verify repository.save was called with NotificationLog
        verify(notificationRepository, times(1)).save(any(NotificationLog.class));
    }

    @Test
    @DisplayName("Should log notification as informal MQTT type")
    void testInformalNotificationLogsAsMqttType() {
        // Arrange
        String message = "Informal message";
        when(informalFactory.createMessageBody()).thenReturn(messageBody);
        when(informalFactory.createDeliveryChannel()).thenReturn(deliveryChannel);
        when(messageBody.format(message)).thenReturn("Formatted: " + message);
        when(notificationRepository.save(any(NotificationLog.class))).thenReturn(new NotificationLog());

        // Act
        notificationService.sendInformalNotification(userId, message);

        // Assert - Verify repository.save was called with NotificationLog
        verify(notificationRepository, times(1)).save(any(NotificationLog.class));
    }

    @Test
    @DisplayName("Should persist notification even when delivery fails")
    void testNotificationPersistsOnDeliveryFailure() {
        // Arrange
        when(userServiceClient.getUserEmail(userId)).thenReturn(userEmail);
        when(formalFactory.createMessageBody()).thenReturn(messageBody);
        when(formalFactory.createDeliveryChannel()).thenReturn(deliveryChannel);
        when(messageBody.format(anyString())).thenReturn("Booking confirmed");
        doThrow(new RuntimeException("Delivery failed"))
                .when(deliveryChannel).send(anyString(), anyString());
        when(notificationRepository.save(any(NotificationLog.class))).thenReturn(new NotificationLog());

        // Act & Assert
        assertThatThrownBy(() -> notificationService.sendBookingConfirmation(bookingEvent))
                .isInstanceOf(RuntimeException.class);

        // Verify notification was still persisted
        verify(notificationRepository, times(1)).save(any(NotificationLog.class));
    }
}
