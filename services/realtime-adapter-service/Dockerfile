# Stage 1: Build with Gradle
FROM eclipse-temurin:21-jdk as builder

WORKDIR /app

# Install Gradle 8.5
RUN apt-get update && apt-get install -y wget unzip && \
    wget https://services.gradle.org/distributions/gradle-8.5-bin.zip && \
    unzip gradle-8.5-bin.zip && \
    rm gradle-8.5-bin.zip

ENV GRADLE_HOME=/app/gradle-8.5
ENV PATH=$GRADLE_HOME/bin:$PATH

# Copy gradle wrapper and build files
COPY . /app

# Build the application
RUN gradle build -p services/realtime-adapter-service -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/services/realtime-adapter-service/build/libs/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
