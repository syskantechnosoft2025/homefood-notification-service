package com.homefood.notification.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private String id;

    private UUID userId;
    private String type; // WELCOME, OTP, LOGIN, ORDER_PLACED, ORDER_CONFIRMED, ORDER_DELIVERED, etc.
    private String channel; // FCM, SMS, EMAIL
    private String title;
    private String body;
    private String data; // JSON
    private boolean isRead;
    private boolean isSent;
    private String failureReason;

    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
}
