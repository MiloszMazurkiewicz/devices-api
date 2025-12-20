# Devices API

This application provides a RESTful API for managing device resources. It supports full CRUD operations with filtering capabilities and enforces business rules around device state management.


## Table of Contents

- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Security Configuration](#security-configuration)
- [Getting Started](#getting-started)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [API Endpoints](#api-endpoints)
- [Domain Model](#domain-model)
- [Business Rules](#business-rules)
- [Testing](#testing)

## Tech Stack

| Component | Technology                       |
|-----------|----------------------------------|
| Language | Java 21                          |
| Framework | Spring Boot 4.0.0                |
| Build Tool | Maven 3.9+                       |
| Database | PostgreSQL 16                    |
| ORM | Spring Data JPA / Hibernate      |
| Migrations | Flyway                           |
| API Docs | springdoc-openapi (OpenAPI 3.0)  |
| Object Mapping | MapStruct                        |
| Testing | JUnit 5, Mockito, Testcontainers |
| Containerization | Docker                           |

## Prerequisites

- Java 21 or higher
- Maven 3.9 or higher
- Docker and Docker Compose (for running with containers)

## Security Configuration

Database credentials use environment variables with sensible defaults for development convenience. The application works out-of-the-box without any configuration.
> **Note:** Default credentials are provided for development/demo purposes only. Never use default credentials in production environments.

## Getting Started

### Clone the repository

```bash
git clone https://github.com/MiloszMazurkiewicz/devices-api
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

The project includes both unit tests and integration tests with Testcontainers.

### Run unit tests only
To run only unit tests (excluding integration tests that require Docker):
```bash
mvn test -Dtest=DeviceControllerTest,DeviceServiceTest
```
This runs 31 tests without requiring Docker.

### Run all tests (including integration tests)

**Prerequisites:**
- Docker must be running
- For Colima users on macOS, set these environment variables:

```bash
export DOCKER_HOST=unix://$HOME/.colima/default/docker.sock
export TESTCONTAINERS_RYUK_DISABLED=true
mvn test
```

Or run in a single command:
```bash
DOCKER_HOST=unix://$HOME/.colima/default/docker.sock TESTCONTAINERS_RYUK_DISABLED=true mvn test
```

For Docker Desktop users:
```bash
mvn test
```

### Test Coverage

The project uses JaCoCo for test coverage reporting. After running tests, view the coverage report at:
```
target/site/jacoco/index.html
```

Current coverage: **95% instruction coverage**, **86% branch coverage**

## License

MIT License
