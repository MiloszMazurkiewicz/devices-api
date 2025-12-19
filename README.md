# Devices API

REST API for managing device resources built with Java 21 and Spring Boot 3.

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [API Endpoints](#api-endpoints)
- [Domain Model](#domain-model)
- [Business Rules](#business-rules)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Future Improvements](#future-improvements)

## Overview

This application provides a RESTful API for managing device resources. It supports full CRUD operations with filtering capabilities and enforces business rules around device state management.

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.3.6 |
| Build Tool | Maven 3.9+ |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA / Hibernate |
| Migrations | Flyway |
| API Docs | springdoc-openapi (OpenAPI 3.0) |
| Object Mapping | MapStruct |
| Testing | JUnit 5, Mockito, Testcontainers |
| Containerization | Docker |

## Prerequisites

- Java 21 or higher
- Maven 3.9 or higher
- Docker and Docker Compose (for running with containers)

## Getting Started

### Clone the repository

```bash
git clone <repository-url>
cd devices-api
```

### Build the project

```bash
mvn clean package
```

### Run tests

```bash
mvn test
```

## Running the Application

### Option 1: Using Docker Compose (Recommended)

This is the easiest way to run the application with all dependencies:

```bash
docker-compose up --build
```

The application will be available at `http://localhost:8080`

To stop:
```bash
docker-compose down
```

To stop and remove volumes:
```bash
docker-compose down -v
```

### Option 2: Running locally with external PostgreSQL

1. Start PostgreSQL (you can use Docker):
```bash
docker run -d \
  --name postgres \
  -e POSTGRES_DB=devicesdb \
  -e POSTGRES_USER=devices \
  -e POSTGRES_PASSWORD=devices \
  -p 5432:5432 \
  postgres:16-alpine
```

2. Run the application:
```bash
mvn spring-boot:run
```

## API Documentation

Once the application is running, you can access:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/devices` | Create a new device |
| GET | `/api/v1/devices/{id}` | Get device by ID |
| GET | `/api/v1/devices` | Get all devices |
| GET | `/api/v1/devices?brand={brand}` | Filter devices by brand |
| GET | `/api/v1/devices?state={state}` | Filter devices by state |
| GET | `/api/v1/devices?brand={brand}&state={state}` | Filter by brand and state |
| PUT | `/api/v1/devices/{id}` | Full update of a device |
| PATCH | `/api/v1/devices/{id}` | Partial update of a device |
| DELETE | `/api/v1/devices/{id}` | Delete a device |

### Example Requests

**Create a device:**
```bash
curl -X POST http://localhost:8080/api/v1/devices \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPhone 15 Pro",
    "brand": "Apple",
    "state": "AVAILABLE"
  }'
```

**Get all devices:**
```bash
curl http://localhost:8080/api/v1/devices
```

**Get devices by brand:**
```bash
curl "http://localhost:8080/api/v1/devices?brand=Apple"
```

**Update device state:**
```bash
curl -X PATCH http://localhost:8080/api/v1/devices/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "state": "IN_USE"
  }'
```

**Delete a device:**
```bash
curl -X DELETE http://localhost:8080/api/v1/devices/{id}
```

## Domain Model

### Device

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Unique identifier (auto-generated) |
| name | String | Device name (required) |
| brand | String | Device brand (required) |
| state | Enum | AVAILABLE, IN_USE, INACTIVE (required) |
| creationTime | Instant | Timestamp of creation (auto-generated, immutable) |

## Business Rules

1. **Creation time cannot be updated** - The creation timestamp is set automatically when a device is created and cannot be modified.

2. **Name and brand cannot be updated if device is in use** - When a device has state `IN_USE`, attempts to update the `name` or `brand` fields will result in a `409 Conflict` error.

3. **Devices in use cannot be deleted** - A device with state `IN_USE` cannot be deleted. Change the state to `AVAILABLE` or `INACTIVE` first.

### Error Responses

The API uses RFC 7807 Problem Details format for error responses:

```json
{
  "type": "https://api.devices.com/errors/device-in-use",
  "title": "Device In Use",
  "status": 409,
  "detail": "Cannot delete device that is in use"
}
```

## Testing

The project includes comprehensive tests at multiple levels:

### Unit Tests
- Service layer tests with Mockito
- Tests for all business rules

```bash
mvn test -Dtest=DeviceServiceTest
```

### Controller Tests
- MockMvc tests for all endpoints
- Request/response validation tests

```bash
mvn test -Dtest=DeviceControllerTest
```

### Integration Tests
- Full end-to-end tests with Testcontainers
- Uses real PostgreSQL database in Docker

```bash
mvn test -Dtest=DeviceIntegrationTest
```

### Run all tests
```bash
mvn test
```

## Project Structure

```
devices-api/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/devices/api/
    │   │   ├── DevicesApiApplication.java
    │   │   ├── config/
    │   │   │   └── OpenApiConfig.java
    │   │   ├── controller/
    │   │   │   └── DeviceController.java
    │   │   ├── dto/
    │   │   │   ├── DeviceRequest.java
    │   │   │   ├── DeviceResponse.java
    │   │   │   └── DeviceUpdateRequest.java
    │   │   ├── entity/
    │   │   │   └── Device.java
    │   │   ├── enums/
    │   │   │   └── DeviceState.java
    │   │   ├── exception/
    │   │   │   ├── DeviceInUseException.java
    │   │   │   ├── DeviceNotFoundException.java
    │   │   │   └── GlobalExceptionHandler.java
    │   │   ├── mapper/
    │   │   │   └── DeviceMapper.java
    │   │   ├── repository/
    │   │   │   └── DeviceRepository.java
    │   │   └── service/
    │   │       ├── DeviceService.java
    │   │       └── DeviceServiceImpl.java
    │   └── resources/
    │       ├── application.yml
    │       ├── application-docker.yml
    │       └── db/migration/
    │           └── V1__create_device_table.sql
    └── test/
        └── java/com/devices/api/
            ├── controller/
            │   └── DeviceControllerTest.java
            ├── integration/
            │   └── DeviceIntegrationTest.java
            ├── repository/
            │   └── DeviceRepositoryTest.java
            └── service/
                └── DeviceServiceTest.java
```

## Future Improvements

1. **Pagination** - Add pagination support for the GET /devices endpoint using Spring Data's `Pageable` interface.

2. **Caching** - Implement Redis caching for frequently accessed devices to improve read performance.

3. **Auditing** - Add Spring Data JPA auditing to track `createdBy`, `updatedAt`, and `updatedBy` fields.

4. **Security** - Implement OAuth2/JWT authentication to secure API endpoints.

5. **Rate Limiting** - Add API rate limiting using Bucket4j or similar library.

6. **Observability** - Add Micrometer metrics and distributed tracing with Spring Cloud Sleuth.

7. **API Versioning** - Implement header-based or media type versioning for future API evolution.

8. **Search** - Add full-text search capabilities using PostgreSQL's built-in search or Elasticsearch.

9. **Events** - Implement domain events using Spring Application Events or a message broker for device state changes.

10. **Health Checks** - Add custom health indicators for external dependencies monitoring.

## License

MIT License
