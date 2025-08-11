# 🚀 RecargaPay Wallet API

## 📌 Overview

The RecargaPay Wallet API is a robust digital wallet service that enables users to manage their financial balance, offering essential operations such as deposit, withdrawal, and fund transfers between users. Built with hexagonal architecture and software engineering best practices, this service ensures high availability, complete traceability, and security in all financial operations.

---

## 📑 Table of Contents

- [🎯 Objectives](#-objectives)
- [🏗️ Architecture](#️-architecture)
- [💻 Technologies](#-technologies)
- [🚀 Getting Started](#-getting-started)
- [🧪 Testing & Quality](#-testing--quality)
- [📝 API Reference](#-api-reference)
- [🛠️ Operations & Monitoring](#️-operations--monitoring)
- [🔒 Security](#-security)
- [📚 Complete Documentation](#-complete-documentation)
- [🌍 Language Versions](#-language-versions)

---

## 🎯 Objectives

This digital wallet service was developed to meet the following requirements:

### Functional Requirements

- **Wallet Creation**: Enable wallet creation for users
- **Balance Inquiry**: Retrieve current wallet balance for a user
- **Historical Balance**: Retrieve wallet balance at a specific point in time
- **Deposit**: Allow fund deposits to the wallet
- **Withdrawal**: Allow fund withdrawals from the wallet
- **Transfer**: Facilitate fund transfers between user wallets

### Non-Functional Requirements

- **High Availability**: Critical service whose unavailability would compromise the platform
- **Traceability**: Complete audit guarantee for all operations to verify balances
- **User Experience**: Detailed and informative responses, including user names
- **Error Handling**: RFC 7807 implementation for all known errors
- **Concurrency**: Isolated transactions to prevent race conditions

---

## 🏗️ Architecture

The project is implemented following **Hexagonal Architecture** (or Ports and Adapters) principles, which allows clear isolation between business rules and infrastructure details.

### Package Structure

```
com.recargapay.wallet/
├── adapter/                     # Adapters (Controllers, Repositories, DTOs)
│   ├── controllers/
│   │   └── v1/                  # REST Controllers v1
│   ├── converters/              # Layer converters (mappers)
│   ├── dtos/                    # Data Transfer Objects
│   ├── entities/                # JPA Entities
│   └── repositories/
│       └── impl/                # Repository implementations
├── core/                        # Business core
│   ├── domain/                  # Domain models
│   ├── exceptions/              # Business exceptions
│   ├── ports/                   # Ports (interfaces)
│   │   ├── in/                  # Input ports (use cases)
│   │   └── out/                 # Output ports (repositories)
│   └── services/                # Use case implementations
│       └── common/              # Common services
└── infra/                       # Infrastructure
    ├── config/                  # Configurations
    ├── handler/                 # Global exception handling
    ├── health/                  # Custom health indicators
    ├── logging/                 # Structured logging
    ├── metrics/                 # Metrics and monitoring
    └── tracing/                 # Distributed tracing
```

### Component Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                  API Layer (Controllers)                     │
│              REST Endpoints + OpenAPI/Swagger                │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                         │
│                 Services (Use Cases)                         │
│          Business Logic + Validation + Caching              │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                            │
│         Entities + Business Rules + Exceptions               │
│              (User, Wallet, Transaction)                     │
└───────────────────────────┬─────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   Infrastructure Layer                       │
│    Database + Cache + Security + Monitoring + Logging       │
│         PostgreSQL + Redis + JWT + Metrics + Tracing        │
└─────────────────────────────────────────────────────────────┘
```

### Architecture Features

- **🔒 Security**: JWT authentication, CORS, security headers
- **📊 Monitoring**: Prometheus metrics, custom health checks
- **📝 Logging**: Structured JSON logging with distributed tracing
- **⚡ Caching**: Redis distributed cache with TTL strategies
- **🔍 Observability**: OpenTelemetry integration for tracing
- **🛡️ Resilience**: Global exception handling and validation

---

## 💻 Technologies

The project uses a modern and robust technology stack:

### Core Framework
- **Java 21**: Main language with advanced features
- **Spring Boot 3.2+**: Application development framework
- **Spring Data JPA**: Simplified data persistence
- **Spring Security**: Authentication and authorization
- **Spring Boot Actuator**: Production-ready features and monitoring

### Database & Persistence
- **PostgreSQL**: Production database
- **H2 Database**: In-memory database for development and testing
- **Flyway**: Database migration management

### Caching & Performance
- **Redis**: Distributed caching system
- **Spring Cache**: Cache abstraction with Redis integration
- **Connection pooling**: Optimized database connections

### Observability & Monitoring
- **OpenTelemetry**: Distributed tracing and observability
- **Structured Logging**: JSON-formatted logs with correlation IDs
- **Grafana**: Visualization and dashboards
- **Loki**: Log aggregation and querying
- **Promtail**: Log collection and forwarding
- **Spring Boot Actuator**: Health checks and metrics

### Security
- **JWT (JSON Web Tokens)**: Stateless authentication
- **OAuth2 Resource Server**: JWT token validation
- **BCrypt**: Password hashing
- **Security Headers**: CSRF, CORS, and security headers
- **Input Validation**: Bean Validation (JSR-303)

### Documentation & API
- **Swagger/OpenAPI 3**: Interactive API documentation
- **Spring REST Docs**: Test-driven documentation

### Development & Quality
- **Maven**: Dependency management and build tool
- **Docker & Docker Compose**: Containerization for development and production
- **JUnit 5**: Testing framework
- **Mockito**: Mocking framework for unit tests
- **Testcontainers**: Integration testing with real databases
- **JaCoCo**: Code coverage analysis
- **SonarQube**: Static code analysis and quality gates

### DevOps & Deployment
- **Docker**: Application containerization
- **Docker Compose**: Multi-container orchestration
- **Environment-specific profiles**: Dev, test, staging, production configurations

---

## 🚀 Getting Started

### Prerequisites

- JDK 21
- Maven 3.8+
- Docker and Docker Compose
- Git

### Cloning the Repository

```bash
git clone https://github.com/your-username/recargapay-wallet-api.git
cd recargapay-wallet-api
```

### Starting with Docker Compose

The project includes Docker Compose configuration for easy development and testing:

1. **Starting services**:

```bash
docker-compose up -d
```

This command will start:
- PostgreSQL database
- Redis cache
- Observability stack (Grafana, Loki, Promtail)
- RecargaPay Wallet API application

2. **Checking services**:

```bash
docker-compose ps
```

You'll see a list of running services and their ports.

### Environment Configuration

Copy the environment template and configure your variables:

```bash
cp .env.template .env
# Edit .env with your specific configurations
```

Key environment variables:
- `DB_HOST`, `DB_PORT`, `DB_NAME`: Database connection
- `JWT_SECRET`: JWT signing secret (minimum 256 bits)
- `REDIS_HOST`, `REDIS_PORT`: Redis cache connection
- `ADMIN_USERNAME`, `ADMIN_PASSWORD`: Default admin user

For detailed environment setup, see: [Environment Configuration](docs/configuration/en/README.md)

---

## 🧪 Testing & Quality

### Running Tests

```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# All tests with coverage
mvn clean test jacoco:report
```

Coverage report will be available at `target/site/jacoco/index.html`

### Code Quality

The project maintains high code quality standards:

- **90%+ test coverage** requirement
- **SonarQube quality gates** for code analysis
- **Ready for CI/CD pipeline integration**

### API Testing

You can use any of these tools to test the API:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Postman**: Import the collection from `docs/api/postman-collection.json`
- **cURL**: Examples in the API documentation

---

## 📝 API Reference

### Authentication

All endpoints require JWT authentication except for the login endpoint.

```bash
# Login to get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin"}'
```

### Core Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/wallets` | Create a new wallet |
| `GET` | `/api/wallets/{userId}/balance` | Get current balance |
| `GET` | `/api/wallets/{userId}/balance?date={date}` | Get historical balance |
| `POST` | `/api/wallets/{userId}/deposit` | Deposit funds |
| `POST` | `/api/wallets/{userId}/withdraw` | Withdraw funds |
| `POST` | `/api/wallets/transfer` | Transfer between wallets |

For detailed API documentation, visit: `http://localhost:8080/swagger-ui.html`

---

## 🛠️ Operations & Monitoring

### Health Checks

The application provides comprehensive health checks:

```bash
# Application health
curl http://localhost:8080/actuator/health

# Detailed health with components
curl http://localhost:8080/actuator/health/detailed
```

### Observability Stack

Access the monitoring tools:

- **Grafana Dashboards**: `http://localhost:3000` (admin/admin)
- **Application Logs**: Structured JSON logs with correlation IDs
- **Metrics**: Available via Spring Boot Actuator endpoints
- **Distributed Tracing**: Request correlation with traceId/spanId

### Log Management

Logs are structured in JSON format for better observability:

- **Local development**: Console output
- **Container deployment**: Available via `docker logs recargapay-wallet-api`
- **Production**: Aggregated in Loki/Grafana stack

Example log entry:
```json
{
  "timestamp": "2025-01-15T10:30:45.123Z",
  "level": "INFO",
  "traceId": "b4ae80e90152b7ab443b5db11e0914b9",
  "spanId": "7f2c1a8b9e3d4c5f",
  "logger": "com.recargapay.wallet.application.service.DepositService",
  "message": "Deposit operation completed successfully",
  "operation": "DEPOSIT",
  "walletId": "123e4567-e89b-12d3-a456-426614174000",
  "amount": 100.00
}
```

For detailed monitoring setup, see: [Observability Documentation](docs/monitoring/en/README.md)

---

## 🔒 Security

The application implements comprehensive security measures:

### Authentication & Authorization
- **JWT-based authentication** with configurable expiration (1 hour default)
- **OAuth2 Resource Server** configuration with JWT token validation
- **Secure password hashing** with BCrypt algorithm
- **Stateless session management** for scalability

### API Security
- **Bean Validation** (`@Valid`) on all controller endpoints with systematic input validation
- **SQL injection prevention** via JPA/Hibernate parameterized queries
- **CORS configuration** with environment-configurable allowed origins, methods, and headers
- **CSRF protection** disabled for stateless API (appropriate for JWT-based authentication)

### Security Headers
- **X-Frame-Options: DENY** - Prevents clickjacking attacks
- **X-Content-Type-Options: nosniff** - Prevents MIME type sniffing
- **X-XSS-Protection: 1; mode=block** - Enables XSS protection
- **Strict-Transport-Security (HSTS)** - Forces HTTPS connections with subdomain inclusion
- **Content-Security-Policy** - Basic CSP for content restriction
- **Referrer-Policy: strict-origin-when-cross-origin** - Controls referrer information
- **Permissions-Policy** - Restricts access to geolocation, microphone, and camera

### Data Protection
- **Sensitive data masking** in structured JSON logs
- **Environment-based secrets** management via `.env` files
- **Request/response tracing** with correlation IDs for audit trails
- **Distributed tracing** with OpenTelemetry for complete request tracking

### Input Validation
- **Systematic validation** across all DTOs:
  - `@NotNull`, `@NotBlank` for required fields
  - `@Email` for email format validation
  - `@Positive` for monetary amounts
  - `@Valid` annotations on all controller methods

For detailed security configuration, see: [Security Documentation](docs/security/en/README.md)

---

## 📚 Complete Documentation

### 🗂️ Documentation Categories

All project documentation is organized by categories, available in English and Portuguese:

| 📂 Category | 🇺🇸 English | 🇧🇷 Português | 📋 Description |
|-------------|-------------|---------------|-------------|
| **⚙️ Configuration** | [Setup Guide](docs/configuration/en/README.md) | [Guia de Configuração](docs/configuration/pt/README.md) | Environment setup, variables, profiles |
| **🚀 Onboarding** | [Team Guide](docs/onboarding/en/README.md) | [Guia do Time](docs/onboarding/pt/README.md) | Developer integration, initial setup |
| **💾 Caching** | [Redis Setup](docs/caching/en/README.md) | [Configuração Redis](docs/caching/pt/README.md) | Distributed cache, TTLs, performance |
| **🔒 Security** | [Security Config](docs/security/en/README.md) | [Configuração Segurança](docs/security/pt/README.md) | JWT, authentication, security headers |
| **📊 Monitoring** | [Observability](docs/monitoring/en/README.md) | [Observabilidade](docs/monitoring/pt/README.md) | Metrics, dashboards, alerts |
| **🔍 Tracing** | [Distributed Tracing](docs/tracing/en/README.md) | [Rastreamento](docs/tracing/pt/README.md) | Structured logs, correlation, debugging |

### 🚀 Quick Start by Role

#### 👨‍💻 New to the project?
1. **[Team Onboarding Guide](docs/onboarding/en/README.md)** - Complete integration checklist
2. **[Local Development Setup](docs/configuration/en/README.md)** - Local environment configuration
3. **[Security Configuration](docs/security/en/README.md)** - Security and JWT setup

#### ⚙️ Setting up environment?
1. **[Environment Setup](docs/configuration/en/README.md)** - Complete configuration guide
2. **[Redis Cache Setup](docs/caching/en/README.md)** - Distributed cache
3. **[Observability Setup](docs/monitoring/en/README.md)** - Monitoring and metrics

#### 🐳 Docker setup?
1. **[Local Development](docs/configuration/en/README.md)** - Docker setup
2. **[Environment Configuration](docs/configuration/en/README.md)** - Container deployment

#### 🔧 Troubleshooting?
1. **[Common Issues](docs/configuration/en/README.md)** - Common problem solutions
2. **[Security Issues](docs/security/en/README.md)** - Security problems
3. **[Cache Issues](docs/caching/en/README.md)** - Cache problems

### 📖 Documentation by Audience

#### For Developers
- **[Team Onboarding](docs/onboarding/en/README.md)** - Complete project integration
- **[Local Setup](docs/configuration/en/README.md)** - Development environment
- **[API Testing](docs/configuration/en/README.md)** - Postman/Insomnia collections
- **[Cache Implementation](docs/caching/en/README.md)** - How to use cache

#### For DevOps/SysAdmin
- **[Environment Setup](docs/configuration/en/README.md)** - Complete configuration
- **[Security Configuration](docs/security/en/README.md)** - Security and compliance
- **[Monitoring Setup](docs/monitoring/en/README.md)** - Observability and alerts
- **[Production Deployment](docs/configuration/en/README.md)** - Production deployment

#### For Architects/Tech Leads
- **[Architecture Overview](#-architecture)** - Hexagonal architecture overview
- **[Distributed Tracing](docs/tracing/en/README.md)** - Tracing and correlation
- **[Performance Optimization](docs/caching/en/README.md)** - Performance optimizations
- **[Security Architecture](docs/security/en/README.md)** - Security architecture

---

## 🌍 Language Versions

- **🇺🇸 English**: You are here!
- **🇧🇷 Português**: [README em Português](docs/README_PT.md)

---

## 📝 Contact

linkedin: https://www.linkedin.com/in/kassimojr/

---
**Built with ❤️ by Kássimo Júnior for RecargaPay Assessment**
