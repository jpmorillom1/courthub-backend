# Realtime Adapter Service

The **Realtime Adapter Service** acts as a bridge between the backend event bus (Kafka) and the frontend clients. It consumes domain events and updates a **Firebase Realtime Database** to push live updates to connected clients (web & mobile).

## 🚀 Features

- **Kafka Consumer**: Listens to critical domain events (`booking.created`, `booking.cancelled`).
- **Firebase Sync**: Updates the Firebase Realtime Database to reflect current court availability.
- **Latency Reduction**: Offloads the "push" responsibility from core services, allowing them to remain stateless and focused on business logic.
- **Actuator Endpoints**: Health checks and Prometheus metrics.

## 🏗️ Architectural Pattern: CQRS

This service implements the **Read Model** of a **CQRS (Command Query Responsibility Segregation)** architecture for the Booking System.

- **Write Model (Command)**: Managed by `booking-service`. It handles complex business rules, validations, and transactional integrity (PostgreSQL) when creating or cancelling bookings.
- **Read Model (Query)**: Managed by `realtime-adapter-service` projection to **Firebase**. It provides low-latency, real-time availability of `time_slots` to thousands of concurrent users without loading the primary database.

1. **Command**: User books a slot via `booking-service` -> PostgreSQL Update.
2. **Event**: `booking.created` event is published to Kafka.
3. **Projection**: `realtime-adapter-service` consumes the event and updates Firebase.
4. **Query**: Clients subscribe to Firebase for instant updates.

## ⏰ Scheduled Jobs

To ensure **Eventual Consistency** (in case of missed events or downtime), a background reconciliation loop runs periodically.

| Job Name | Schedule | Description |
|----------|----------|-------------|
| **Availability Reconciliation** | `0 */15 * * * *` (Every 15 minutes) | Syncs the next 7 days of slots from `booking-service` (PostgreSQL) -> Firebase. This "heals" any data drift. |

## 🛠️ Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.2.x
- **Integration**: Spring Kafka
- **Push Service**: Firebase Admin SDK
- **Build Tool**: Gradle 8.5
- **Service Discovery**: Netflix Eureka Client
- **Containerization**: Docker

## 📋 Prerequisites

Before running the service, ensure you have the following installed:

- [Java Development Kit (JDK) 21](https://adoptium.net/)
- [Gradle 8.5](https://gradle.org/install/) (or use the provided wrapper)
- [Kafka](https://kafka.apache.org/) (9092)
- [Firebase Project](https://firebase.google.com/) (with Realtime Database enabled)
- [Docker](https://www.docker.com/) (optional)

## ⚙️ Configuration

The service implements **Externalized Configuration**.

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka Brokers | `kafka:9092` |
| `FIREBASE_DATABASE_URL` | URL of your Firebase Realtime DB | *Required* |
| `FIREBASE_CREDENTIALS_PATH` | Path to `service-account.json`.| *Required* |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Eureka Server URL | `http://localhost:8761/eureka/` |
| `SERVER_PORT` | Application server port | `8084` |

> [!IMPORTANT]
> You must provide a valid Firebase Service Account JSON file and mount it to the container or provide its path locally.

## 🚀 Installation & Running

### 1. using Gradle (Local)

1.  **Clone the repository** (if not already done).
2.  **Navigate to the service directory**:
    ```bash
    cd services/realtime-adapter-service
    ```
3.  **Place your Firebase Credentials**:
    Save your `service-account.json` in a secure location (e.g., `src/main/resources` for dev, but **gitignored**).
4.  **Build the project**:
    ```bash
    gradle build -x test
    ```
5.  **Run the application**:
    ```bash
    export FIREBASE_DATABASE_URL=https://your-project.firebaseio.com
    export FIREBASE_CREDENTIALS_PATH=classpath:service-account.json
    gradle bootRun
    ```
    The service will start on port **8084**.

### 2. using Docker

1.  **Build the Docker image**:
    ```bash
    docker build -t courthub/realtime-adapter-service .
    ```
2.  **Run the container**:
    ```bash
    docker run -p 8084:8084 \
      -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
      -e FIREBASE_DATABASE_URL=https://your-project.firebaseio.com \
      -e FIREBASE_CREDENTIALS_PATH=/app/secrets/service-account.json \
      -v /path/to/local/service-account.json:/app/secrets/service-account.json \
      courthub/realtime-adapter-service
    ```

## 📡 Event-Driven Architecture

This service is a **Pure Consumer**. It does not produce events or expose REST APIs for business logic.

### Consumed Events

| Event Topic | Logic | Firebase Update |
|-------------|-------|-----------------|
| `booking.created` | New booking confirmed | Sets the slot status to `BOOKED` at `availabilities/{courtId}/{date}/{startTime}` |
| `booking.cancelled` | Booking cancelled | Sets the slot status to `AVAILABLE` to make it visible to clients immediately. |

## 🔍 Monitoring & Health

The service exposes Spring Boot Actuator endpoints:

- **Health Check**: `http://localhost:8084/actuator/health`
- **Prometheus Metrics**: `http://localhost:8084/actuator/prometheus`

## 🧪 Testing

Run the unit and integration tests using Gradle:

```bash
gradle test
```
