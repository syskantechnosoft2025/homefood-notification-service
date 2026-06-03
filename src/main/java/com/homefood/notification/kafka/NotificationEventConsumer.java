package com.homefood.notification.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homefood.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "notification.send", groupId = "notification-service")
    public void handleNotificationEvent(@Payload String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String type = node.get("type").asText();

            switch (type) {
                case "WELCOME" -> {
                    UUID userId = UUID.fromString(node.get("userId").asText());
                    String email = node.get("email").asText();
                    String firstName = node.get("firstName").asText();
                    notificationService.sendWelcomeEmail(userId, email, firstName);
                }
                case "OTP" -> {
                    String phone = node.get("phone").asText();
                    String otp = node.get("otp").asText();
                    notificationService.sendOtpSms(phone, otp);
                }
                case "ORDER_PLACED", "ORDER_CONFIRMED", "ORDER_DELIVERED", "RATE_ORDER",
                     "PAYMENT_PROCESSED", "REFUND_INITIATED" -> {
                    UUID userId = UUID.fromString(node.get("userId").asText());
                    String orderId = node.has("orderId") ? node.get("orderId").asText() : "";
                    notificationService.sendOrderNotification(userId, type, orderId,
                            getMessageForType(type, node));
                }
                case "LOGIN" -> log.debug("Login event for user: {}", node.get("userId").asText());
                default -> log.debug("Unhandled notification type: {}", type);
            }
        } catch (Exception e) {
            log.error("Error processing notification event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "order.delivered", groupId = "notification-service-delivered")
    public void handleOrderDelivered(@Payload String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            UUID buyerId = UUID.fromString(node.get("buyerId").asText());
            String orderId = node.get("orderId").asText();
            notificationService.sendOrderNotification(buyerId, "ORDER_DELIVERED", orderId,
                    "Your order has been delivered! Rate your experience.");
        } catch (Exception e) {
            log.error("Error handling order.delivered notification: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "payment.refund", groupId = "notification-service-refund")
    public void handleRefund(@Payload String message) {
        try {
            JsonNode node = objectMapper.readTree(message);
            String orderId = node.get("orderId").asText();
            log.info("Refund initiated for order: {} — notification handled", orderId);
        } catch (Exception e) {
            log.error("Error handling refund notification: {}", e.getMessage());
        }
    }

    private String getMessageForType(String type, JsonNode node) {
        return switch (type) {
            case "ORDER_PLACED" -> "Your order has been placed successfully!";
            case "ORDER_CONFIRMED" -> "Your order is confirmed! Chef is preparing your food.";
            case "ORDER_DELIVERED" -> "Your order has been delivered! Enjoy your meal!";
            case "RATE_ORDER" -> "How was your experience? Rate your order now.";
            case "PAYMENT_PROCESSED" -> "Payment of ₹" + (node.has("amount") ? node.get("amount").asText() : "") + " received.";
            case "REFUND_INITIATED" -> "Your refund has been initiated. It will reflect in 3-5 business days.";
            default -> "You have a new update from HomeFOOD.";
        };
    }
}
