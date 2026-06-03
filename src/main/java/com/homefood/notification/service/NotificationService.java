package com.homefood.notification.service;

import com.homefood.notification.entity.Notification;
import com.homefood.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    @Value("${notification.from-email:noreply@homefood.com}")
    private String fromEmail;

    @Value("${notification.fcm.server-key:mock-fcm-key}")
    private String fcmServerKey;

    public void sendWelcomeEmail(UUID userId, String email, String firstName) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type("WELCOME")
                .channel("EMAIL")
                .title("Welcome to HomeFOOD!")
                .body("Hi " + firstName + "! Welcome to the HomeFOOD family. Discover amazing homemade meals near you.")
                .isSent(false)
                .build();

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Welcome to HomeFOOD!");
            message.setText("Hi " + firstName + ",\n\nWelcome to HomeFOOD! " +
                    "Discover amazing homemade meals near you.\n\nHappy eating!\nThe HomeFOOD Team");
            mailSender.send(message);
            notification.setIsSent(true);
            notification.setSentAt(LocalDateTime.now());
            log.info("Welcome email sent to: {}", email);
        } catch (Exception e) {
            notification.setFailureReason(e.getMessage());
            log.warn("Failed to send welcome email: {}", e.getMessage());
        }

        notificationRepository.save(notification);
    }

    public void sendOtpSms(String phone, String otp) {
        // In production: integrate with Twilio/AWS SNS
        Notification notification = Notification.builder()
                .type("OTP")
                .channel("SMS")
                .title("HomeFOOD OTP")
                .body("Your HomeFOOD OTP is: " + otp + ". Valid for 5 minutes. Do not share with anyone.")
                .isSent(true)
                .sentAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        log.info("OTP SMS sent to: {}", phone.replaceAll("(\\d{3})\\d{4}(\\d{3})", "$1****$2"));
    }

    public void sendOrderNotification(UUID userId, String type, String orderId, String message) {
        // In production: use FCM SDK
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .channel("FCM")
                .title(getTitleForType(type))
                .body(message)
                .data("{\"orderId\":\"" + orderId + "\"}")
                .isSent(true)
                .sentAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        log.info("Push notification {} sent to user: {}", type, userId);
    }

    public void sendGenericNotification(UUID userId, String type, String channel, String title, String body) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .channel(channel)
                .title(title)
                .body(body)
                .isSent(true)
                .sentAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            n.setReadAt(LocalDateTime.now());
            notificationRepository.save(n);
        });
    }

    private String getTitleForType(String type) {
        return switch (type) {
            case "ORDER_PLACED" -> "Order Placed!";
            case "ORDER_CONFIRMED" -> "Order Confirmed!";
            case "ORDER_PREPARING" -> "Chef is preparing your food";
            case "FOOD_PICKED_UP" -> "Delivery agent picked up your order";
            case "ORDER_DELIVERED" -> "Order Delivered!";
            case "RATE_ORDER" -> "How was your meal?";
            case "PAYMENT_PROCESSED" -> "Payment Successful";
            case "REFUND_INITIATED" -> "Refund Initiated";
            default -> "HomeFOOD Update";
        };
    }
}
