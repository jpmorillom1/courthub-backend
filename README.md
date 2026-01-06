# CourtHub Backend — Monorepo (Spring Boot)

Backend for **CourtHub**, a web application for **sports court reservation and management** at the **Central University of Ecuador (UCE)**.

This project is structured as a **monorepo** based on **Spring Boot** and **Java 21**, designed to evolve into a **microservices-based architecture** with shared components.

> ⚠️ **Project status:** initial (0).  
> The base structure is defined and services will be added progressively.

---

## Purpose

Provide a unified backend repository for CourtHub that enables:

- Scalable microservices architecture
- Reusable shared libraries
- Independent service development and deployment
- Clear and maintainable project structure

---

## General structure


```
courthub-backend/
┣ libs/ # Shared libraries (config, security, DTOs, etc.)
┣ services/ # System microservices
┣ build.gradle
┣ settings.gradle
┗ gradlew / gradlew.bat  
```

---

## Core technologies

- Java 21
- Spring Boot
- Gradle (monorepo)
- Microservices architecture
- JWT / OAuth (planned)
- Docker (planned)

---

## Notes

- Each microservice is developed independently while sharing common modules.
- The initial focus is on simplicity and clean structure.
- Documentation and services will expand as development progresses.

---

📍 **Central University of Ecuador**  
📘 Academic Project – CourtHub Backend
