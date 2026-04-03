# Notification_Service

Node.js microservice for handling user and internal notifications in the Hotel Booking Demo system.

## Features

- Internal HTTP API for sending notifications
- Channel abstraction for `EMAIL` and `TELEGRAM`
- Template-based notification rendering
- Basic retry with exponential backoff
- Basic in-memory idempotency store
- Health check endpoint
- RabbitMQ-ready structure for future integration

## Quick Start

```bash
cd Notification_Service
cp .env.example .env
npm install
npm run dev
```

## Endpoints

- `GET /health`
- `POST /api/notifications/send`

## Notes

- When `MAIL_ENABLED=false` or `TELEGRAM_ENABLED=false`, the service performs dry-run logging for local development.
- `idempotency.service.js` is intentionally isolated so it can later be swapped with Redis or a database implementation.
- `rabbitmq.consumer.js` and `event.publisher.js` are starter extension points for event-driven integration.
