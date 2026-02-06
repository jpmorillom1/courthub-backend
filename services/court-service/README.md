# Court Service

The **Court Service** is responsible for managing sports facilities (courts) within the **CourtHub** platform. It handles court creation, scheduling, status updates, and issue reporting. It integrates **Redis** for high-performance caching and **Kafka** for event-driven communication.

## 🚀 Features

- **Court Management**: Create, update, and retrieve courts with filtering (Sport Type, Surface Type, Status).
- **Schedule Management**: Manage court availability and schedules.
- **Issue Reporting**: Users can report issues (incidents) for specific courts.
- **Admin Tools**: Manage issue severity and resolution status.
- **High Performance Caching**: Implements **Cache-Aside** pattern using **Redis** for frequent queries.
- **Event Streaming**: Publishes domain events via **Kafka** (e.g., `court.created`, `court.status.changed`).
- **Actuator Endpoints**: Health checks and Prometheus metrics.

## 📡 Event-Driven Architecture

This service uses **Apache Kafka** to broadcast domain events to other microservices (e.g., `reservation-service` for availability checks).

### Produced Events

The service acts as a **Producer** for the following events. All events are published **transactionally** (only after the DB transaction commits).

| Event Topic | Trigger | Payload Description |
|-------------|---------|---------------------|
| `court.created` | New court is registered | Contains full court details (`id`, `name`, `location`, `sportType`, etc.) |
| `court.updated` | Court details are modified | Contains updated court details. |
| `court.status.changed` | Court status changes (e.g., to `MAINTENANCE`) | Contains `id` and new `status`. Important for preventing new reservations. |
| `court.schedule.updated` | Operating hours are changed | Contains schedule details (`dayOfWeek`, `openTime`, `closeTime`) and court context. |

### Consumed Events

*This service currently does not consume any Kafka events.*

## 🛠️ Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.2.x
- **Build Tool**: Gradle 8.5
- **Database**: PostgreSQL
- **Cache**: Redis
- **Message Broker**: Apache Kafka
- **Service Discovery**: Netflix Eureka Client
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Containerization**: Docker

## 📋 Prerequisites

Before running the service, ensure you have the following installed:

- [Java Development Kit (JDK) 21](https://adoptium.net/)
- [Gradle 8.5](https://gradle.org/install/) (or use the provided wrapper)
- [PostgreSQL](https://www.postgresql.org/) (5432)
- [Redis](https://redis.io/) (6379)
- [Kafka](https://kafka.apache.org/) (9092)
- [Docker](https://www.docker.com/) (optional)

## ⚙️ Configuration

The service implements **Externalized Configuration**.

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `SPRING_DATASOURCE_URL` | JDBC URL for PostgreSQL | `jdbc:postgresql://localhost:5432/courthub_court` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `postgres` |
| `REDIS_HOST` | Redis Host | `localhost` |
| `REDIS_PORT` | Redis Port | `6379` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka Brokers | `localhost:9092` |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Eureka Server URL | `http://localhost:8761/eureka/` |
| `SERVER_PORT` | Application server port | `8082` |

## 🚀 Installation & Running

### 1. using Gradle (Local)

1.  **Clone the repository** (if not already done).
2.  **Navigate to the service directory**:
    ```bash
    cd services/court-service
    ```
3.  **Build the project**:
    ```bash
    gradle build -x test
    ```
4.  **Run the application**:
    ```bash
    gradle bootRun
    ```
    The service will start on port **8082**.

### 2. using Docker

1.  **Build the Docker image**:
    ```bash
    docker build -t courthub/court-service .
    ```
2.  **Run the container**:
    ```bash
    docker run -p 8082:8082 \
      -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/courthub_court \
      -e REDIS_HOST=host.docker.internal \
      -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
      courthub/court-service
    ```

## 📡 API Endpoints

### Court Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/courts/hello` | Health check. |
| `GET` | `/courts/sports` | List available sport types. |
| `POST` | `/courts` | **Create Court** (Admin). |
| `GET` | `/courts` | List courts (supports filtering by sport, surface, status). |
| `GET` | `/courts/{id}` | Get court details by ID. |
| `PATCH`| `/courts/{id}/status` | Update court status. |
| `POST` | `/courts/{courtId}/schedule` | Upsert court schedule. |

### Issue/Incident Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/courts/issues/severity-levels` | List issue severity levels. |
| `POST` | `/courts/{courtId}/issues` | **Report Issue** for a court. |
| `GET` | `/courts/{courtId}/issues` | List issues for a specific court. |
| `GET` | `/courts/issues/{issueId}` | Get issue details. |
| `GET` | `/courts/issues` | **List Pending Issues** (Admin Only). |
| `PATCH`| `/courts/issues/{issueId}/status` | Update issue status (Admin Only). |

## ⏰ Scheduled Jobs

This service executes background tasks using Spring Scheduler.

| Job Name | Schedule | Description |
|----------|----------|-------------|
| **Weekly Schedule Creation** | `0 0 0 * * MON` (Every Monday at midnight) | Generates court schedules for the *next* week (Mon-Sun) for all ACTIVE courts. |

## 🧠 Caching Strategy

This service uses **Redis** with a **Cache-Aside** strategy to optimize read operations.

- **Keys Used**:
  - `courts::list-{sportType}-{surfaceType}-{status}`: Cached court lists.
  - `courts::{UUID}`: Cached single court details.
- **Invalidation**: Queries invalidating the cache (Create, Update) trigger a full flush of the related cache entries to ensure consistency.

## 🔍 Monitoring & Health

The service exposes Spring Boot Actuator endpoints:

- **Health Check**: `http://localhost:8082/actuator/health`
- **Prometheus Metrics**: `http://localhost:8082/actuator/prometheus`
- **Swagger UI**: `http://localhost:8082/swagger-ui.html` (if enabled)

## 🧪 Testing

Run the unit and integration tests using Gradle:

```bash
gradle test
```
