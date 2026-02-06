# User Service

The **User Service** is a core microservice of the **CourtHub** platform responsible for managing user identities, profiles, and registration. It provides RESTful APIs for creating, retrieving, and updating user information, as well as internal endpoints for credential validation and analytics.

## 🚀 Features

- **User Registration**: Create new user accounts with validation.
- **Profile Management**: Retrieve and update user details.
- **Authentication Support**: Validate credentials and identify current users via JWT.
- **Internal APIs**: Support for other microservices (e.g., Auth Service) to validate user credentials.
- **Actuator Endpoints**: Health checks and Prometheus metrics for monitoring.

## 🛠️ Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.2.x
- **Build Tool**: Gradle 8.5
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA (Hibernate)
- **Service Discovery**: Netflix Eureka Client
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Containerization**: Docker

## 📋 Prerequisites

Before running the service, ensure you have the following installed:

- [Java Development Kit (JDK) 21](https://adoptium.net/)
- [Gradle 8.5](https://gradle.org/install/) (or use the provided wrapper)
- [PostgreSQL](https://www.postgresql.org/) (running on port 5432)
- [Docker](https://www.docker.com/) (optional, for containerized execution)

## ⚙️ Configuration

The service implements **Externalized Configuration**. You can configure the application using the following environment variables.

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `SPRING_DATASOURCE_URL` | JDBC URL for the PostgreSQL database | `jdbc:postgresql://localhost:5432/courthub_user` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `postgres` |
| `JWT_SECRET` | Secret key for JWT signing/verification | *configured default* |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Eureka Server URL | `http://localhost:8761/eureka/` |
| `SERVER_PORT` | Application server port | `8081` |

## 🚀 Installation & Running

### 1. using Gradle (Local)

1.  **Clone the repository** (if not already done).
2.  **Navigate to the service directory**:
    ```bash
    cd services/user-service
    ```
3.  **Build the project**:
    ```bash
    gradle build -x test
    ```
4.  **Run the application**:
    ```bash
    gradle bootRun
    ```
    The service will start on port **8081**.

### 2. using Docker

1.  **Build the Docker image**:
    ```bash
    docker build -t courthub/user-service .
    ```
2.  **Run the container**:
    ```bash
    docker run -p 8081:8081 \
      -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/courthub_user \
      -e DB_USERNAME=myuser \
      -e DB_PASSWORD=mypassword \
      courthub/user-service
    ```

## 📡 API Endpoints

### Public Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/users/hello` | Health check / Hello message. |
| `POST` | `/users` | **Registration**. Create a new user account. |

### Protected Endpoints (Requires Bearer Token)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/users/me` | Get details of the currently authenticated user. |
| `GET` | `/users/{id}` | Get user details by ID. |
| `GET` | `/users/email/{email}` | Get user details by email. |
| `PUT` | `/users/{id}` | Update user information. |

### Internal Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/users/validate-credentials` | Validate email and password (used by Auth Service). |
| `GET` | `/users/internal/users/all` | Retrieve all users (for Internal Analytics). |

## 🔍 Monitoring & Health

The service exposes Spring Boot Actuator endpoints:

- **Health Check**: `http://localhost:8081/actuator/health`
- **Prometheus Metrics**: `http://localhost:8081/actuator/prometheus`
- **Swagger UI**: `http://localhost:8081/swagger-ui.html` (if enabled)

## 🧪 Testing

Run the unit and integration tests using Gradle:

```bash
gradle test
```
