# Notifications service

An event-driven microservice built with Spring Boot and RabbitMQ, designed to process and dispatch notifications 
asynchronously. Whether it's a booking or a payment confirmation, this service handles template rendering and
delivery seamlessly.

## Key Features
* Asynchronous Processing: Powered by RabbitMQ to handle high-throughput notification events without blocking upstream services.

* Strategy Pattern Architecture: Easily scalable codebase supporting multiple notification providers (currently integrated with Google/Gmail API).

* Dynamic Templating: Utilizes the Mustache Engine to render clean, responsive HTML emails out of classpath:templates/.

* Fail-Safe Error Routing: Out-of-the-box resiliency featuring Dead Letter Queues (DLQ) and message Time-To-Live (TTL).

* Robust Input Validation: Strict contract enforcement via Jakarta Validation (@Valid) directly on RabbitMQ listeners.


## Prerequisites

* Java 21
* RabbitMQ 
* Docker & Docker Compose
* Keycloak
* A valid GCP OAuth credential file for Gmail API access.

## Environment configuration

Create a `.env` file in the root directory based on the example below:

```bash
# --- Google/Email Configs --- 
CRED_FILE=/creds.json
GOOGLE_APPLICATION_CREDENTIALS=/YOUR_PATH_TO_GCP_OAUTH_SECRET/creds.json

SENDER_EMAIL=sgonzalezd2007@gmail.com

# --- KeyCloak  --- 
KEYCLOAK_HOST=localhost

# --- Logging  --- 
LOKI_HOST=localhost;

# --- RabbitMQ Config --- 
RABBITMQ_HOST=localhost
RABBITMQ_USER=admin
RABBITMQ_PASSWORD=YOUR_PASSWORD

# --- Redis Config ---
REDIS_HOST=localhost
REDIS_PASSWORD=YOUR_REDIS_PASSWORD
```

---

## Messaging Architecture
This service consumes messages from a dedicated Topic Exchange and routes them through a resilient queue infrastructure.

| Component | Default Configuration Property | Description |
| --- | --- | --- |
| **Exchange** | `queueProperties.getExchangeName()` | Topic Exchange handling incoming notifications. |
| **Main Queue** | `queueProperties.getQueueName()` | Durable queue with configured TTL. |
| **DLQ Exchange** | `queueProperties.getDlqExchangeName()` | Direct Exchange for routing failed messages. |
| **Dead Letter Queue** | `queueProperties.getDlqName()` | Holds messages that failed validation or threw unrecoverable errors. |

## Usage & Integration
To send a notification through this service, publish a JSON payload to the configured RabbitMQ exchange using the routing key.
Message Schema (NotificationDTO)

Your payload must match the following structure:
```json
{
  "notificationId": "a9e6b218-9c14-4b53-90be-8451f22dc05d",
  "notificationType": "TICKET_CONFIRMATION",
  "subject": "Your Cinema Ticket Confirmation!",
  "provider": "GOOGLE",
  "recipient": "watcher@example.com",
  "templateModel": {
    "recipient": "recipient@something.com",
    "customerName": "John Doe",
    "movieTitle": "Interstellar",
    "seatNumber": "H-12",
    "showtime": "2026-07-15 19:30"
  }
}
```

**Note on Templates**: The `notificationType` field maps directly to your template files. For instance, `"notificationType": "TICKET_CONFIRMATION"` expects a template named `TICKET_CONFIRMATION.mustache` inside your `src/main/resources/templates/` folder.

## Error Handling

### 1. Messaging Failures & DLQ

If an incoming message lacks required fields (like a missing `recipient` or `notificationId`), the validator rejects it instantly.

* Uncaught technical issues or explicit strategy mismatches throw an `AmqpRejectAndDontRequeueException`.
* This safely ejects the toxic payload out of the main loop and routes it straight into the **Dead Letter Queue (DLQ)** for manual inspection, avoiding infinite retry loops.

### 2. Standardized Application Errors

For general business and internal technical exceptions, the service responds with a standardized `BaseErrorDto`:

```json
{
  "message": "Something went wrong while sending the email: Connection timed out",
  "code": "TECHNICAL_ERROR",
  "status": "INTERNAL_SERVER_ERROR"
}

```

### Common Error Classifications

* `VALIDATION_ERROR` / `BAD_REQUEST`: Payload failed structural validation constraints.
* `TECHNICAL_ERROR`: Issues involving template parsing (`IOException`) or external mail provider downtime.
