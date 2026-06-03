# Notification Service

Listens to all Kafka topics and sends FCM push notifications, SMS (Twilio), and email (SMTP).

## Channels
- **FCM** — Push notifications for mobile/web
- **SMS** — OTP, order updates via Twilio/AWS SNS
- **Email** — Welcome, receipts via SMTP/SES

## Kafka Topics Consumed
- `notification.send` — Generic notification requests
- `order.delivered` — Delivery completion
- `payment.refund` — Refund notifications
