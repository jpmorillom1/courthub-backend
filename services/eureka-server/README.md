# Eureka Server

The **Eureka Server** is the **Service Discovery** component of the CourtHub microservices ecosystem. It acts as a registry where all backend services (`user-service`, `court-service`, etc.) register themselves, allowing them to locate and communicate with each other dynamically without hardcoded URLs.

## 🚀 Features

- **Service Registry**: Maintains a live list of active microservice instances.
- **Heartbeat Monitoring**: Periodically checks service health; removes dead instances from the registry.
- **Load Balancing Support**: Enables client-side load balancing for services communicating via OpenFeign or WebClient.
- **Dashboard**: Provides a web interface to view running services and their status.

## 🛠️ Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.2.x
- **Component**: Spring Cloud Netflix Eureka Server
- **Build Tool**: Gradle 8.5
- **Containerization**: Docker

## 📋 Prerequisites

Before running the server, ensure you have the following installed:

- [Java Development Kit (JDK) 21](https://adoptium.net/)
- [Gradle 8.5](https://gradle.org/install/) (or use the provided wrapper)
- [Docker](https://www.docker.com/) (optional)

## ⚙️ Configuration

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `SERVER_PORT` | Application server port | `8761` |
| `EUREKA_CLIENT_REGISTER_WITH_EUREKA` | Self-registration (Disabled for Server) | `false` |
| `EUREKA_CLIENT_FETCH_REGISTRY` | Fetch registry (Disabled for Server) | `false` |

## 🚀 Installation & Running

### 1. using Gradle (Local)

1.  **Clone the repository** (if not already done).
2.  **Navigate to the service directory**:
    ```bash
    cd services/eureka-server
    ```
3.  **Run the application**:
    ```bash
    gradle bootRun
    ```
    The server will start on port **8761**.

### 2. using Docker

1.  **Build the Docker image**:
    ```bash
    docker build -t courthub/eureka-server .
    ```
2.  **Run the container**:
    ```bash
    docker run -p 8761:8761 courthub/eureka-server
    ```

## 🔍 Dashboard

Once running, access the Eureka Dashboard to monitor registered services:

**URL**: `http://localhost:8761/`

You should see a table listing all connected services (e.g., `USER-SERVICE`, `COURT-SERVICE`) and their status (`UP`).
