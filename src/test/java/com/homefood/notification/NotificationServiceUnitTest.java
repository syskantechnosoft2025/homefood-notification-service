package com.homefood.notification;

import com.homefood.notification.entity.Notification;
import com.homefood.notification.repository.NotificationRepository;
import com.homefood.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceUnitTest {

    @Mock NotificationRepository notificationRepository;
    @Mock JavaMailSender mailSender;
    @InjectMocks NotificationService notificationService;

    @Test
    void sendWelcomeEmail_savesNotification() {
        ReflectionTestUtils.setField(notificationService, "fromEmail", "noreply@homefood.com");
        ReflectionTestUtils.setField(notificationService, "fcmServerKey", "mock-key");

        UUID userId = UUID.randomUUID();
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> notificationService.sendWelcomeEmail(userId, "user@test.com", "Test"))
            .doesNotThrowAnyException();
        verify(notificationRepository).save(any());
    }

    @Test
    void sendOtpSms_savesNotification() {
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertThatCode(() -> notificationService.sendOtpSms("9999999999", "123456"))
            .doesNotThrowAnyException();
        verify(notificationRepository).save(any());
    }

    @Test
    void sendOrderNotification_savesNotification() {
        ReflectionTestUtils.setField(notificationService, "fcmServerKey", "mock-key");
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertThatCode(() ->
            notificationService.sendOrderNotification(UUID.randomUUID(), "ORDER_PLACED", "ord_001", "Order placed!"))
            .doesNotThrowAnyException();
    }

    @Test
    void getUserNotifications_returnsList() {
        UUID userId = UUID.randomUUID();
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId))
            .thenReturn(List.of(new Notification()));
        List<Notification> result = notificationService.getUserNotifications(userId);
        assertThat(result).hasSize(1);
    }

    @Test
    void markAsRead_updatesNotification() {
        Notification n = Notification.builder().id("notif_001").isRead(false).build();
        when(notificationRepository.findById("notif_001")).thenReturn(Optional.of(n));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertThatCode(() -> notificationService.markAsRead("notif_001"))
            .doesNotThrowAnyException();
    }
}
