# Build stage
FROM gradle:8.5-jdk21 AS build

WORKDIR /app

# Copy Gradle files
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Copy all source code
COPY libs ./libs
COPY services/user-service ./services/user-service

# Build the project
RUN gradle :services:user-service:build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR
COPY --from=build /app/services/user-service/build/libs/*.jar app.jar

# Expose port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

