# Notification Service

The **Notification Service** is responsible for delivering timely updates to users across multiple channels (Email, MQTT). It abstracts the complexity of notification delivery using design patterns and integrates with external providers like **HiveMQ** and **SMTP** servers.

## 🚀 Features

- **Multi-Channel Delivery**:
  - **Email (SMTP)**: For formal communications like payment confirmations.
  - **MQTT (HiveMQ)**: For instant, informal alerts like "Payment Expired".
- **Design Pattern**: Implements the **Abstract Factory Pattern** to decouple notification logic from delivery mechanisms.
- **Event-Driven**: Consumes Kafka events to trigger notifications independently.
- **Audit Logging**: Persists notification history in **MongoDB**.
- **Actuator Endpoints**: Health checks and Prometheus metrics.

## 🏗️ Design Pattern: Abstract Factory

This service uses the **Abstract Factory Pattern** to create families of related objects (Message Body, Delivery Channel) without specifying their concrete classes.

| Factory Type | Channel | Use Case |
|--------------|---------|----------|
| **FormalNotificationFactory** | `EmailChannel` (SMTP) | Used for **Payment Confirmations**. Generates formal HTML/Text emails. |
| **InformalNotificationFactory** | `MqttChannel` (HiveMQ) | Used for **Payment Expiration**. Pushes lightweight JSON alerts to mobile/web clients. |

### Implementation Details
- **Interface**: `NotificationFactory`
- **Products**: `MessageBody`, `DeliveryChannel`
- **Benefits**: Easily extensible. If we need SMS in the future, we simply add an `SmsNotificationFactory`.

## 🛠️ Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.2.x
- **Message Broker (Input)**: Apache Kafka
- **Database (Logs)**: MongoDB
- **MQTT Broker**: HiveMQ Cloud (Paho Client)
- **Email**: JavaMailSender (SMTP)
- **Build Tool**: Gradle 8.5
- **Service Discovery**: Netflix Eureka Client
- **Containerization**: Docker

## 📋 Prerequisites

Before running the service, ensure you have the following installed:

- [Java Development Kit (JDK) 21](https://adoptium.net/)
- [Gradle 8.5](https://gradle.org/install/) (or use the provided wrapper)
- [MongoDB](https://www.mongodb.com/) (27017)
- [Kafka](https://kafka.apache.org/) (9092)
- [HiveMQ Cloud Account](https://www.hivemq.com/) (Broker URL & Credentials)
- [SMTP Server](https://mailtrap.io/) (Host, Port, Auth)
- [Docker](https://www.docker.com/) (optional)

## ⚙️ Configuration

The service implements **Externalized Configuration**.

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka Brokers | `localhost:9092` |
| `MONGODB_URI` | MongoDB Connection URI | `mongodb://localhost:27017/courthub_notification` |
| `MAIL_HOST` | SMTP Server Host | *Required* |
| `MAIL_PORT` | SMTP Server Port | *Required* |
| `MAIL_USERNAME` | SMTP Username | *Required* |
| `MAIL_PASSWORD` | SMTP Password | *Required* |
| `HIVEMQ_BROKER_URL` | MQTT Broker URL (ssl://...) | *Required* |
| `HIVEMQ_USERNAME` | HiveMQ Username | *Required* |
| `HIVEMQ_PASSWORD` | HiveMQ Password | *Required* |
| `HIVEMQ_TOPIC` | Root topic for MQTT | `courthub` |

## 🚀 Installation & Running

### 1. using Gradle (Local)

1.  **Clone the repository** (if not already done).
2.  **Navigate to the service directory**:
    ```bash
    cd services/notification-service
    ```
3.  **Configure Environment Variables**:
    Ensure all SMTP and HiveMQ headers are set in your environment or `application.yml`.

4.  **Run the application**:
    ```bash
    gradle bootRun
    ```
    The service will start on port **8085**.

### 2. using Docker

1.  **Build the Docker image**:
    ```bash
    docker build -t courthub/notification-service .
    ```
2.  **Run the container**:
    ```bash
    docker run -p 8085:8085 \
      -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
      -e MONGODB_URI=mongodb://host.docker.internal:27017/courthub_notification \
      -e MAIL_HOST=smtp.mailtrap.io \
      -e HIVEMQ_BROKER_URL=ssl://your-cluster.hivemq.cloud:8883 \
      courthub/notification-service
    ```

## 📡 Event-Driven Architecture

### Consumed Events

| Event Topic | Logic | Pattern/Channel |
|-------------|-------|-----------------|
| `payment.confirmed` | Payment successful | **Formal Factory** -> **Email**. User receives a receipt. |
| `payment.expired` | Payment window timed out | **Informal Factory** -> **MQTT**. User gets a popup alert. |

## 🔍 Monitoring & Health

The service exposes Spring Boot Actuator endpoints:

- **Health Check**: `http://localhost:8085/actuator/health`
- **Prometheus Metrics**: `http://localhost:8085/actuator/prometheus`

## 🧪 Testing

Run the unit and integration tests using Gradle:

```bash
gradle test
```
