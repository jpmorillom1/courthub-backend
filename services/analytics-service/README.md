# Analytics Service

The **Analytics Service** is the Business Intelligence unit of CourtHub. It implements an **ETL (Extract, Transform, Load)** pipeline to aggregate data from core microservices, processing it into actionable **KPIs** for the dashboard.

## 🚀 Features

- **ETL Pipeline**: Aggregates distributed data into a centralized `MongoDB` data warehouse.
- **KPI Generation**: Computes metrics like Occupancy Rates, Peak Hours, and Faculty Usage.
- **Scheduled Sync**: Periodic data reconciliation to keep metrics fresh.
- **Microservice Integration**: Feign Clients connect to `user-service`, `booking-service`, and `court-service`.
- **Actuator Endpoints**: Health checks and Prometheus metrics.

## 📊 Calculated KPIs

The service processes raw data into the following metrics:

1.  **Occupancy Rate**: Percentage of utilization per court and hour.
2.  **Peak Hours**: Heatmap of most requested times and days.
3.  **Faculty Usage**: Breakdown of bookings by user faculty (Engineering, Medicine, Arts, etc.).
4.  **Student Ranking**: Top active users by confirmed bookings.
5.  **Maintenance Stats**: Overview of court issues by severity (Critical, High, Medium, Low).
6.  **Reservation History**: Monthly trend of completed vs. cancelled bookings.

## ⚙️ Cron Job (The ETL Process)

A background job runs periodically to trigger the Extract-Transform-Load cycle.

| Job Name | Schedule | Description |
|----------|----------|-------------|
| **Data Synchronization** | `0 0 * * * *` (Every Hour) | 1. **Extract**: Pulls all bookings, users, and issues via Feign Clients.<br>2. **Transform**: Aggregates, filters (Confirmed status), and calculates derived stats.<br>3. **Load**: Upserts results into MongoDB collections for fast Dashboard queries. |

## 🛠️ Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.2.x
- **Data Warehouse**: MongoDB
- **Communication**: OpenFeign (Synchronous REST)
- **Scheduling**: Spring `@Scheduled`
- **Build Tool**: Gradle 8.5
- **Service Discovery**: Netflix Eureka Client
- **Containerization**: Docker

## 📋 Prerequisites

Before running the service, ensure you have the following installed:

- [Java Development Kit (JDK) 21](https://adoptium.net/)
- [Gradle 8.5](https://gradle.org/install/) (or use the provided wrapper)
- [MongoDB](https://www.mongodb.com/) (27017)
- [Docker](https://www.docker.com/) (optional)

## ⚙️ Configuration

The service implements **Externalized Configuration**.

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `MONGODB_URI` | MongoDB Connection URI | `mongodb://localhost:27017/courthub_analytics` |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Eureka Server URL | `http://localhost:8761/eureka/` |
| `SERVER_PORT` | Application server port | `8087` |

## 🚀 Installation & Running

### 1. using Gradle (Local)

1.  **Clone the repository** (if not already done).
2.  **Navigate to the service directory**:
    ```bash
    cd services/analytics-service
    ```
3.  **Run the application**:
    ```bash
    gradle bootRun
    ```
    The service will start on port **8087**.

### 2. using Docker

1.  **Build the Docker image**:
    ```bash
    docker build -t courthub/analytics-service .
    ```
2.  **Run the container**:
    ```bash
    docker run -p 8087:8087 \
      -e MONGODB_URI=mongodb://host.docker.internal:27017/courthub_analytics \
      courthub/analytics-service
    ```

## 📡 API Endpoints

The service primarily exposes data via REST for the Dashboard Frontend.

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/analytics/occupancy` | Get occupancy rates by date range. |
| `GET` | `/api/analytics/faculty` | Get usage distribution by faculty. |
| `GET` | `/api/analytics/peak-hours` | Get heat map data for peak hours. |
| `GET` | `/api/analytics/ranking` | Get top student users. |

## 🔍 Monitoring & Health

The service exposes Spring Boot Actuator endpoints:

- **Health Check**: `http://localhost:8087/actuator/health`
- **Prometheus Metrics**: `http://localhost:8087/actuator/prometheus`

## 🧪 Testing

Run the unit and integration tests using Gradle:

```bash
gradle test
```
