# Auth Service

The **Auth Service** is the central security component of the **CourtHub** platform. It handles user authentication, token generation (JWT), and authorization flows. It supports both standard email/password login and OAuth2 (Google) authentication.

## 🚀 Features

- **JWT Authentication**: Secure token generation (Access & Refresh Tokens).
- **Standard Login**: Authenticate using email and password.
- **OAuth2 Support**: Login with Google.
- **Token Management**: Endpoints to refresh expired access tokens.
- **Service Security**: Centralized logic for issuing and validating identities.
- **Actuator Endpoints**: Health checks and Prometheus metrics for monitoring.

## 🛠️ Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.2.x
- **Security**: Spring Security 6, OAuth2 Client
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
| `SPRING_DATASOURCE_URL` | JDBC URL for the PostgreSQL database | `jdbc:postgresql://localhost:5432/courthub_auth` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `postgres` |
| `JWT_SECRET` | Secret key for JWT signing (HS256) | *configured default* |
| `GOOGLE_CLIENT_ID` | Google OAuth2 Client ID | *Required* |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 Client Secret | *Required* |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Eureka Server URL | `http://localhost:8761/eureka/` |
| `SERVER_PORT` | Application server port | `8080` |

> [!IMPORTANT]
> You must provide valid `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` to enable Google Login features.

## 🚀 Installation & Running

### 1. using Gradle (Local)

1.  **Clone the repository** (if not already done).
2.  **Navigate to the service directory**:
    ```bash
    cd services/auth-service
    ```
3.  **Build the project**:
    ```bash
    gradle build -x test
    ```
4.  **Run the application**:
    ```bash
    gradle bootRun
    ```
    The service will start on port **8080**.

### 2. using Docker

1.  **Build the Docker image**:
    ```bash
    docker build -t courthub/auth-service .
    ```
2.  **Run the container**:
    ```bash
    docker run -p 8080:8080 \
      -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/courthub_auth \
      -e DB_USERNAME=myuser \
      -e DB_PASSWORD=mypassword \
      -e GOOGLE_CLIENT_ID=your_google_client_id \
      -e GOOGLE_CLIENT_SECRET=your_google_client_secret \
      courthub/auth-service
    ```

## 📡 API Endpoints

All authentication endpoints are grouped under `/auth`.

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/auth/hello` | Health check / Hello message. |
| `POST` | `/auth/login` | **Login**. Authenticate with email/password to get tokens. |
| `POST` | `/auth/refresh` | **Refresh Token**. Get a new access token using a refresh token. |
| `GET/POST`| `/auth/oauth2/login` | **OAuth2 Callback**. Endpoint for Google OAuth2 authentication flow. |
| `GET` | `/auth/oauth2/error` | Redirect target for OAuth2 failures. |

## 🔍 Monitoring & Health

The service exposes Spring Boot Actuator endpoints:

- **Health Check**: `http://localhost:8080/actuator/health`
- **Prometheus Metrics**: `http://localhost:8080/actuator/prometheus`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html` (if enabled)

## 🧪 Testing

Run the unit and integration tests using Gradle:

```bash
gradle test
```
