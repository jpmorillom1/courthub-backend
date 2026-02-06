# CourtHub Backend

Backend for **CourtHub**, a comprehensive web application for **sports court reservation and management** at the **Central University of Ecuador (UCE)**.

This project is structured as a **Monorepo** and uses a scalable **Microservices Architecture** powered by **Spring Boot**, **Kafka**, and **Docker**.

---

## 🏗️ Architecture

The system is composed of loose-coupled microservices communicating via **REST (OpenFeign)** and **Events (Kafka)**.

### Core Services

| Service | Feature Description | README |
|---------|---------------------|--------|
| **[api-gateway](./services/api-gateway)** | Unified entry point, routing (`Port 9000`), & Load Balancing. | [Read](./services/api-gateway/README.md) |
| **[eureka-server](./services/eureka-server)** | Service Registry & Discovery (`Port 8761`). | [Read](./services/eureka-server/README.md) |
| **[user-service](./services/user-service)** | User management, profiles, and roles. | [Read](./services/user-service/README.md) |
| **[auth-service](./services/auth-service)** | Authentication (JWT) & OAuth2 (Google). | [Read](./services/auth-service/README.md) |
| **[court-service](./services/court-service)** | Court Inventory, status management, and **Redis Caching**. | [Read](./services/court-service/README.md) |
| **[booking-service](./services/booking-service)** | Reservation logic using **PostgreSQL** transactions. | [Read](./services/booking-service/README.md) |
| **[payment-service](./services/payment-service)** | **Stripe** integration & **SAGA** Pattern orchestration. | [Read](./services/payment-service/README.md) |
| **[notification-service](./services/notification-service)** | **Abstract Factory** for Email (SMTP) & Realtime (MQTT) alerts. | [Read](./services/notification-service/README.md) |
| **[analytics-service](./services/analytics-service)** | **ETL Pipeline** for Dashboard KPIs & Reporting. | [Read](./services/analytics-service/README.md) |
| **[realtime-adapter-service](./services/realtime-adapter-service)** | **CQRS** Read Model projecting data to **Firebase**. | [Read](./services/realtime-adapter-service/README.md) |

---

## 📦 Monorepo Strategy (Gradle)

We adopted a **Monorepo** structure managed by **Gradle Multi-Module** to efficiently handle the complexity of distributed systems.

### Why Monorepo?
- **Shared Libraries**: Critical components are centralized in the `libs/` directory and reused across services as dependencies.
    - `common-dto`: Unified Data Transfer Objects for consistent API contracts.
    - `common-security`: Reusable JWT filters and Security Configurations.
    - `common-exception`: Standardized error handling and global exception advice.
    - `common-web`: Shared web utilities.
- **Consistency**: Ensures all services use compatible versions of dependencies (Spring Boot, Cloud, etc.) via the root `build.gradle`.
- **Developer Velocity**: Easier to refactor across service boundaries and run integration tests in a single environment.

---

## 🧩 Key Patterns & Technologies

- **Microservices Architecture**: Independent, autonomously deployable services organized around business capabilities.
- **Event-Driven Architecture**: Asynchronous communication using **Apache Kafka** topics (`booking.created`, `payment.confirmed`, etc.).
- **Distributed SAGA**: Ensures data consistency across `booking` and `payment` services.
- **CQRS (Command Query Responsibility Segregation)**:
    - **Write**: `booking-service` (PostgreSQL).
    - **Read**: `realtime-adapter-service` (Firebase Realtime DB).
- **Abstract Factory Pattern**: Decouples notification logic from delivery channels (SMTP/MQTT).
- **Observability Stack**:
    - **Promtail**: Agent that scrapes logs from all containers.
    - **Loki**: Log aggregation system (like Prometheus, but for logs).
    - **Grafana**: Dashboard for visualizing logs and metrics.
- **ETL & Cron Jobs**: Scheduled tasks for data aggregation (`analytics-service`) and consistency checks (`realtime-adapter`, `court-service`).

---

## 🚀 Workflows & Infrastructure (AWS EC2)

The infrastructure is designed to be deployed on **AWS EC2** instances using **Terraform** for provisioning and **GitHub Actions** for CI/CD.

### Infrastructure Segregation
1.  **Core Services**: `auth`, `user`, `court`, `booking` -> Deployed on optimized compute instances.
2.  **Async/Event Services**: `notification`, `realtime-adapter`, `analytics` -> Deployed on instances optimizing IO/Network.
3.  **Data Layer**: RDS (PostgreSQL), ElastiCache (Redis), MSK (Kafka) or self-hosted equivalents.
4.  **Networking**:
    - **Public Subnet**: Application Load Balancer (ALB), Bastion Host.
    - **Private Subnet**: All Microservices, Databases.

### CI/CD Pipeline (GitHub Actions)

We utilize **GitHub Actions** to automate the build and deployment process:

1.  **Build & Test**:
    - On `push` to `main`, Gradle builds all services and runs unit tests.
2.  **Dockerize**:
    - Builds Docker images for each service.
    - Pushes images to **Amazon ECR** or **Docker Hub**.
3.  **Deploy**:
    - SSH into EC2 instances via Bastion.
    - Pulls latest Docker images.
    - Restarts containers using `docker-compose` or Orchestrator.

---

## 🛠️ Getting Started (Local Dev)

### Prerequisites
- Java 21
- Docker & Docker Compose
- Gradle 8.5

### Running the System
You can run the entire stack using Docker Compose:

```bash
docker-compose up -d
```

Or run individual services via Gradle:

```bash
# Terminal 1: Service Registry
cd services/eureka-server && gradle bootRun

# Terminal 2: API Gateway
cd services/api-gateway && gradle bootRun

# Terminal 3: Auth Service
cd services/auth-service && gradle bootRun
# ... and so on
```

---

