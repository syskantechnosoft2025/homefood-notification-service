package com.homefood.notification;

import com.homefood.notification.controller.NotificationController;
import com.homefood.notification.entity.Notification;
import com.homefood.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerUnitTest {

    @Mock NotificationService notificationService;
    @InjectMocks NotificationController notificationController;

    @Test
    void getNotifications_returns200() {
        UUID userId = UUID.randomUUID();
        when(notificationService.getUserNotifications(userId))
            .thenReturn(List.of(new Notification()));

        ResponseEntity<List<Notification>> result = notificationController.getNotifications(userId);
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).hasSize(1);
    }
}
