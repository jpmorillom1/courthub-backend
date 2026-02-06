# API Gateway

The **API Gateway** is the single entry point for all client requests in the CourtHub architecture. Implemented using **Spring Cloud Gateway**, it handles routing, load balancing, and cross-cutting concerns (like security headers) for the backend microservices.

## 🚀 Features

- **Unified Entry Point**: Routes requests to appropriate microservices based on URL paths.
- **Dynamic Routing**: Integration with **Eureka Server** (`lb://`) for client-side load balancing.
- **Microservices Proxy**: Hides the internal architecture from the frontend.
- **Actuator Endpoints**: Health checks and Prometheus metrics.

## 🛠️ Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.2.x
- **Component**: Spring Cloud Gateway
- **Service Discovery**: Netflix Eureka Client
- **Build Tool**: Gradle 8.5
- **Containerization**: Docker

## 📋 Prerequisites

Before running the gateway, ensure you have the following installed:

- [Java Development Kit (JDK) 21](https://adoptium.net/)
- [Gradle 8.5](https://gradle.org/install/) (or use the provided wrapper)
- [Eureka Server](https://github.com/jpmorillom1/courthub-backend/tree/main/services/eureka-server) (Running on 8761)
- [Docker](https://www.docker.com/) (optional)

## ⚙️ Configuration

The gateway is configured to run on port **9000**.

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `SERVER_PORT` | Application server port | `9000` |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Eureka Server URL | `http://localhost:8761/eureka/` |

### Route Configuration

The gateway automatically routes requests to registered services:

| Path Pattern | Target Service ID | Description |
|--------------|-------------------|-------------|
| `/auth/**` | `AUTH-SERVICE` | Authentication & Authorization |
| `/users/**` | `USER-SERVICE` | User Management |
| `/courts/**` | `COURT-SERVICE` | Court Management |
| `/bookings/**` | `BOOKING-SERVICE` | Booking Logic |
| `/api/payments/**` | `PAYMENT-SERVICE` | Payments & Webhooks |
| `/api/notifications/**`| `NOTIFICATION-SERVICE`| Notification Management |
| `/analytics/**` | `ANALYTICS-SERVICE` | Dashboard & KPIs |

## 🚀 Installation & Running

### 1. using Gradle (Local)

1.  **Clone the repository** (if not already done).
2.  **Navigate to the service directory**:
    ```bash
    cd services/api-gateway
    ```
3.  **Run the application**:
    ```bash
    gradle bootRun
    ```
    The gateway will start on port **9000**.

### 2. using Docker

1.  **Build the Docker image**:
    ```bash
    docker build -t courthub/api-gateway .
    ```
2.  **Run the container**:
    ```bash
    docker run -p 9000:9000 \
      -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://host.docker.internal:8761/eureka/ \
      courthub/api-gateway
    ```

## 🔍 Monitoring & Health

The service exposes Spring Boot Actuator endpoints:

- **Health Check**: `http://localhost:9000/actuator/health`
- **Prometheus Metrics**: `http://localhost:9000/actuator/prometheus`
