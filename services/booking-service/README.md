# Booking Service

The **Booking Service** is the engine of the **CourtHub** platform. It manages the reservation lifecycle, availability checking, and ensures data consistency across the distributed system using **Kafka**.

## 🚀 Features

- **Reservation Management**: Create, retrieve, and cancel bookings.
- **Availability Availability**: Real-time query of available time slots for courts.
- **Event-Driven Consistency**: Updates local court snapshots based on events from `court-service` and processes payment results from `payment-service`.
- **Sync API**: Internal endpoints for synchronizing slot states.
- **Actuator Endpoints**: Health checks and Prometheus metrics.

- **Actuator Endpoints**: Health checks and Prometheus metrics.

## ⏰ Scheduled Jobs

This service executes background tasks to maintain data hygiene.

| Job Name | Schedule | Description |
|----------|----------|-------------|
| **Old Slots Cleanup** | `0 0 2 * * *` (Daily at 02:00 AM) | Deletes unused (AVAILABLE) time slots from previous days to save database space. |

## 📡 Event-Driven Architecture

This service relies heavily on **Apache Kafka** to maintain state without direct synchronous coupling to other services.

### Produced Events

The service acts as a **Producer** for:

| Event Topic | Trigger | Payload Description |
|-------------|---------|---------------------|
| `booking.created` | new booking is requested (Status: `PENDING_PAYMENT`) | Contains `bookingId`, `userId`, `courtId`, `amount`, and `timeSlot` details. Triggers Payment Service. |
| `booking.cancelled` | Booking is cancelled by user | Contains `bookingId` to notify other services (e.g., to process refunds if applicable). |

### Consumed Events

The service acts as a **Consumer** for:

| Event Topic | Source Service | Action Taken |
|-------------|----------------|--------------|
| `payment.confirmed` | Payment Service | Updates booking status to `CONFIRMED`. |
| `payment.failed` | Payment Service | Updates booking status to `PAYMENT_FAILED`. |
| `payment.expired` | Payment Service | Marks booking as expired and releases the slot. |
| `court.created` | Court Service | Creates a local snapshot of the court for faster availability checks. |
| `court.updated` | Court Service | Updates local court snapshot. |
| `court.status.changed` | Court Service | Updates local court status (e.g., closes slots if status becomes `MAINTENANCE`). |
| `court.schedule.updated` | Court Service | Updates operating hours in the local snapshot. |

## 🛠️ Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.2.x
- **Build Tool**: Gradle 8.5
- **Database**: PostgreSQL
- **Message Broker**: Apache Kafka
- **Service Discovery**: Netflix Eureka Client
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Containerization**: Docker

## 📋 Prerequisites

Before running the service, ensure you have the following installed:

- [Java Development Kit (JDK) 21](https://adoptium.net/)
- [Gradle 8.5](https://gradle.org/install/) (or use the provided wrapper)
- [PostgreSQL](https://www.postgresql.org/) (5432)
- [Kafka](https://kafka.apache.org/) (9092)
- [Docker](https://www.docker.com/) (optional)

## ⚙️ Configuration

The service implements **Externalized Configuration**.

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `SPRING_DATASOURCE_URL` | JDBC URL for PostgreSQL | `jdbc:postgresql://localhost:5432/courthub_booking` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `postgres` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka Brokers | `localhost:9092` |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Eureka Server URL | `http://localhost:8761/eureka/` |
| `SERVER_PORT` | Application server port | `8083` |
| `SLOT_DURATION_MINUTES` | Length of a booking slot | `60` |
| `SLOT_GENERATION_DAYS_FORWARD` | How many days ahead to generate slots | `7` |

## 🚀 Installation & Running

### 1. using Gradle (Local)

1.  **Clone the repository** (if not already done).
2.  **Navigate to the service directory**:
    ```bash
    cd services/booking-service
    ```
3.  **Build the project**:
    ```bash
    gradle build -x test
    ```
4.  **Run the application**:
    ```bash
    gradle bootRun
    ```
    The service will start on port **8083**.

### 2. using Docker

1.  **Build the Docker image**:
    ```bash
    docker build -t courthub/booking-service .
    ```
2.  **Run the container**:
    ```bash
    docker run -p 8083:8083 \
      -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/courthub_booking \
      -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
      courthub/booking-service
    ```

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/bookings/hello` | Health check. |
| `POST` | `/bookings` | **Create Booking**. Reserves a slot and initiates payment. |
| `GET` | `/bookings/availability` | Get available slots for a court and date. |
| `GET` | `/bookings/{id}` | Get booking details. |
| `GET` | `/bookings/user/{userId}` | Get all bookings for a user. |
| `PATCH`| `/bookings/{id}/cancel` | Cancel a booking. |
| `GET` | `/bookings/internal/slots-sync` | Internal: Sync all slots for a date. |

## 🔍 Monitoring & Health

The service exposes Spring Boot Actuator endpoints:

- **Health Check**: `http://localhost:8083/actuator/health`
- **Prometheus Metrics**: `http://localhost:8083/actuator/prometheus`
- **Swagger UI**: `http://localhost:8083/swagger-ui.html` (if enabled)

## 🧪 Testing

Run the unit and integration tests using Gradle:

```bash
gradle test
```
