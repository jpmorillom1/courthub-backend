# Payment Service

The **Payment Service** handles financial transactions for CourtHub. It integrates with **Stripe** to process secure payments and participates in a distributed **SAGA** transaction to ensure booking consistency.

## 🚀 Features

- **Stripe Integration**: Generates secure Checkout Sessions for users.
- **Webhook Handling**: Listens for asynchronous payment results from Stripe (`completed`, `expired`).
- **Distributed Saga**: Orchestrates the payment step of the Booking Saga.
- **Event-Driven**: Fully asynchronous communication with other microservices.
- **Actuator Endpoints**: Health checks and Prometheus metrics.

## 🔄 Distributed SAGA Pattern

This service acts as a critical participant in the **Booking Creation Saga**. It ensures that a booking is only confirmed when payment is successfully captured.

### Workflow

1.  **Trigger**: Receives `booking.created` event from `booking-service`.
2.  **Action**: Creates a Stripe Checkout Session and saves a `PENDING` payment record.
3.  **User Interaction**: User pays via the Stripe hosted page.
4.  **Completion**:
    *   **Success**: Stripe sends `checkout.session.completed` webhook -> Service updates status to `COMPLETED` -> Publishes `payment.confirmed`.
    *   **Failure/Expiration**: Stripe sends `checkout.session.expired` webhook -> Service updates status to `EXPIRED` -> Publishes `payment.expired`.
5.  **Compensation**: The `booking-service` listens to these events to either **Confirm** the booking or **Release** the time slot.

## 🛠️ Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.2.x
- **Payment Gateway**: Stripe Java SDK
- **Message Broker**: Apache Kafka
- **Database**: PostgreSQL
- **Build Tool**: Gradle 8.5
- **Service Discovery**: Netflix Eureka Client
- **Containerization**: Docker

## 📋 Prerequisites

Before running the service, ensure you have the following installed:

- [Java Development Kit (JDK) 21](https://adoptium.net/)
- [Gradle 8.5](https://gradle.org/install/) (or use the provided wrapper)
- [PostgreSQL](https://www.postgresql.org/) (5432)
- [Kafka](https://kafka.apache.org/) (9092)
- [Stripe Account](https://stripe.com/) (API Keys & Webhooks)
- [Docker](https://www.docker.com/) (optional)

## ⚙️ Configuration

The service implements **Externalized Configuration**.

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka Brokers | `localhost:9092` |
| `SPRING_DATASOURCE_URL` | JDBC URL for PostgreSQL | `jdbc:postgresql://...` |
| `STRIPE_API_KEY` | **Secret Key** from Stripe Dashboard | *Required* |
| `STRIPE_WEBHOOK_SECRET` | **Webhook Secret** (whsec_...) | *Required* |
| `STRIPE_PRICE_PER_HOUR` | Cost of booking (in cents) | `1000` ($10.00) |
| `STRIPE_SUCCESS_URL` | Redirect after payment | `http://.../success` |
| `STRIPE_CANCEL_URL` | Redirect after cancellation | `http://.../cancel` |

> [!IMPORTANT]
> The `STRIPE_WEBHOOK_SECRET` is critical. You must use the Stripe CLI or Dashboard to generate this secret for your local environment.

## 🚀 Installation & Running

### 1. using Gradle (Local)

1.  **Clone the repository** (if not already done).
2.  **Navigate to the service directory**:
    ```bash
    cd services/payment-service
    ```
3.  **Setup Stripe CLI** (for local webhooks):
    ```bash
    stripe login
    stripe listen --forward-to localhost:8086/api/payment/webhook/stripe
    ```
    *Copy the webhook secret printed by the CLI.*

4.  **Run the application**:
    ```bash
    export STRIPE_API_KEY=sk_test_...
    export STRIPE_WEBHOOK_SECRET=whsec_...
    gradle bootRun
    ```
    The service will start on port **8086**.

### 2. using Docker

1.  **Build the Docker image**:
    ```bash
    docker build -t courthub/payment-service .
    ```
2.  **Run the container**:
    ```bash
    docker run -p 8086:8086 \
      -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
      -e STRIPE_API_KEY=sk_test_... \
      -e STRIPE_WEBHOOK_SECRET=whsec_... \
      courthub/payment-service
    ```

## 📡 Event-Driven Architecture

### Produced Events

| Event Topic | Trigger | Payload |
|-------------|---------|---------|
| `payment.confirmed` | Successful Stripe Webhook | `bookingId`, `amount`, `status: COMPLETED` |
| `payment.failed` | Failed Payment | `bookingId`, `reason`, `status: FAILED` |
| `payment.expired` | Session Expired Webhook | `bookingId`, `status: EXPIRED` |

### Consumed Events

| Event Topic | Source Service | Action Taken |
|-------------|----------------|--------------|
| `booking.created` | Booking Service | Initiates Stripe Checkout Session. |

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/payment/webhook/stripe` | **Stripe Webhook**. Handles `checkout.session.*` events. |
| `GET` | `/api/payment/payments/user/{userId}` | Get payment history for a user. |

## 🔍 Monitoring & Health

The service exposes Spring Boot Actuator endpoints:

- **Health Check**: `http://localhost:8086/actuator/health`
- **Prometheus Metrics**: `http://localhost:8086/actuator/prometheus`

## 🧪 Testing

Run the unit and integration tests using Gradle:

```bash
gradle test
```
