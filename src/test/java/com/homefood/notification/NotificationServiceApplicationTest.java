package com.homefood.notification;

import com.homefood.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NotificationServiceApplicationTest {

    @Mock
    NotificationRepository notificationRepository;

    @Test
    void contextLoads() {
        assertThat(notificationRepository).isNotNull();
    }
}
