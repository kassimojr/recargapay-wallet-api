# Configuration Reference Guide

## Overview

This document provides a comprehensive reference for all configuration properties used in the Digital Wallet API project. Each property is documented with its purpose, impact, and relationship to other configurations.

## Table of Contents

- [Application Configuration (application.yml)](#application-configuration-applicationyml)
- [Monitoring Stack Configuration](#monitoring-stack-configuration)
- [Configuration Dependencies](#configuration-dependencies)
- [Environment-Specific Settings](#environment-specific-settings)

---

## Application Configuration (application.yml)

### Spring Framework Core

#### spring.application.name
- **Type**: string
- **Current Value**: `digital-wallet-api`
- **Description**: Defines the application name used across Spring Boot components
- **Purpose**: Used in logging patterns, metrics labels, and distributed tracing to identify this service
- **Impact**: If removed, default service identification will be lost in logs and monitoring
- **Relationships**: Referenced in logging.pattern.console and otel.service.name

#### spring.datasource.url
- **Type**: string
- **Current Value**: `jdbc:postgresql://localhost:5432/walletdb`
- **Description**: JDBC URL for PostgreSQL database connection
- **Purpose**: Primary database connection for wallet and transaction data persistence
- **Impact**: Application cannot start without valid database connection
- **Relationships**: Must match PostgreSQL service configuration in docker-compose.yml

#### spring.datasource.username/password
- **Type**: string
- **Current Value**: `admin`/`admin`
- **Description**: Database credentials for authentication
- **Purpose**: Authenticates with PostgreSQL database
- **Impact**: Database connection will fail with incorrect credentials
- **Environment**: Development (hardcoded), Production (should use secrets)

### JPA/Hibernate Configuration

#### spring.jpa.hibernate.ddl-auto
- **Type**: string
- **Current Value**: `validate`
- **Description**: Hibernate schema management strategy
- **Purpose**: Validates database schema against entity mappings without modifying it
- **Impact**: Prevents accidental schema changes, ensures Flyway manages all migrations
- **Critical**: Must remain 'validate' to work with Flyway

#### spring.jpa.show-sql
- **Type**: boolean
- **Current Value**: `true`
- **Description**: Enables SQL statement logging
- **Purpose**: Helps debug database queries during development
- **Environment**: Development (true), Production (should be false)

### Flyway Migration

#### spring.flyway.enabled
- **Type**: boolean
- **Current Value**: `true`
- **Description**: Enables Flyway database migration
- **Purpose**: Manages database schema evolution
- **Impact**: Database schema won't be managed if disabled
- **Critical**: Required for schema management

#### spring.flyway.locations
- **Type**: array
- **Current Value**: `classpath:db/migration`
- **Description**: Location of migration scripts
- **Purpose**: Points to SQL migration files in src/main/resources/db/migration
- **Impact**: Migrations won't be found if path is wrong

### Security Configuration

#### spring.security.oauth2.resourceserver.jwt.secret
- **Type**: string
- **Current Value**: `Q4!z8@pW#r2$Lm9^X7eF%uS6bT1&cV0*Y3jH`
- **Description**: Secret key for JWT token validation
- **Purpose**: Validates JWT tokens in authentication
- **Impact**: Authentication will fail without valid secret
- **Used By**: AuthController, JwtDecoderConfig, TestJwtConfig
- **Environment**: Development (hardcoded), Production (should use secrets)

### Documentation (SpringDoc)

#### springdoc.api-docs.enabled
- **Type**: boolean
- **Current Value**: `true`
- **Description**: Enables OpenAPI documentation generation
- **Purpose**: Provides interactive API documentation via Swagger UI
- **Impact**: API docs won't be available if disabled

#### springdoc.swagger-ui.path
- **Type**: string
- **Current Value**: `/swagger-ui.html`
- **Description**: Path for Swagger UI interface
- **Purpose**: Interactive API documentation accessible at http://localhost:8080/swagger-ui.html
- **Relationships**: Whitelisted in SecurityConfig

### OpenTelemetry Configuration

#### otel.sdk.disabled
- **Type**: boolean
- **Current Value**: `true`
- **Description**: Disables OpenTelemetry SDK auto-configuration
- **Purpose**: Uses manual configuration instead of automatic instrumentation
- **Impact**: Prevents automatic instrumentation conflicts
- **Relationships**: Allows custom TraceContextFilter to work properly

#### otel.service.name
- **Type**: string
- **Current Value**: `digital-wallet-api`
- **Description**: Service name for distributed tracing
- **Purpose**: Identifies service in distributed traces and logs
- **Relationships**: Should match spring.application.name

#### otel.propagators
- **Type**: string
- **Current Value**: `tracecontext,baggage`
- **Description**: W3C standard trace context propagation formats
- **Purpose**: Enables distributed tracing across services
- **Used By**: TraceContextFilter for trace propagation

### Logging Configuration

#### logging.pattern.console
- **Type**: string
- **Current Value**: `%d{yyyy-MM-dd HH:mm:ss.SSS} [${spring.application.name:-wallet-api}] [%X{traceId:-},%X{spanId:-}] [%thread] %-5level %logger{36} - %msg%n`
- **Description**: Console log format pattern with trace correlation
- **Purpose**: Structured logging that includes traceId and spanId for correlation
- **Impact**: Essential for distributed tracing and log correlation
- **Relationships**: Uses spring.application.name and MDC values from TraceContextFilter

#### logging.level.com.digital.wallet
- **Type**: string
- **Current Value**: `DEBUG`
- **Description**: Application-specific log level
- **Purpose**: Detailed logging for application code debugging
- **Environment**: Development (DEBUG), Production (INFO recommended)

### Actuator/Monitoring

#### management.endpoints.web.exposure.include
- **Type**: array
- **Current Value**: `health,info,prometheus,metrics,loggers,env`
- **Description**: Exposed actuator endpoints for monitoring
- **Purpose**: Enables health checks, metrics collection, and runtime management
- **Impact**: Endpoints not listed won't be accessible
- **Used By**: Prometheus scraping, Kubernetes health checks

#### management.endpoint.health.show-details
- **Type**: string
- **Current Value**: `always`
- **Description**: Health endpoint detail level
- **Purpose**: Shows detailed health information for debugging
- **Environment**: Development (always), Production (when-authorized recommended)

#### management.endpoint.prometheus.enabled
- **Type**: boolean
- **Current Value**: `true`
- **Description**: Enables Prometheus metrics endpoint
- **Purpose**: Provides metrics for Prometheus scraping at /actuator/prometheus
- **Critical**: Required for monitoring stack integration

---

## Monitoring Stack Configuration

### Loki Configuration (loki-config.yaml)

#### auth_enabled
- **Type**: boolean
- **Current Value**: `false`
- **Description**: Disables authentication for Loki
- **Purpose**: Simplifies development environment setup
- **Environment**: Development (false), Production (true recommended)

#### server.http_listen_port
- **Type**: integer
- **Current Value**: `3100`
- **Description**: HTTP server port for Loki
- **Purpose**: Port for log ingestion and queries
- **Relationships**: Referenced in docker-compose.yml port mapping and Promtail client configuration

#### limits_config.max_streams_per_user
- **Type**: integer
- **Current Value**: `10000`
- **Description**: Maximum streams per user to prevent cardinality explosion
- **Purpose**: Prevents "Maximum active stream limit exceeded" errors
- **Critical**: Essential for stable log ingestion with high-cardinality labels

#### limits_config.ingestion_rate_mb
- **Type**: integer
- **Current Value**: `4`
- **Description**: Ingestion rate limit in MB/s
- **Purpose**: Controls log ingestion throughput to prevent overload
- **Impact**: Logs may be rejected if rate is exceeded

### Promtail Configuration (promtail-config.yaml)

#### server.http_listen_port
- **Type**: integer
- **Current Value**: `9080`
- **Description**: HTTP server port for Promtail
- **Purpose**: Avoids port conflicts with other services

#### clients[0].url
- **Type**: string
- **Current Value**: `http://loki:3100/loki/api/v1/push`
- **Description**: Loki endpoint for log shipping
- **Purpose**: Sends collected logs to Loki
- **Relationships**: Must match Loki service name and port in docker-compose.yml

#### scrape_configs[0].static_configs[0].labels.__path__
- **Type**: string
- **Current Value**: `/app/logs/wallet-api*.json`
- **Description**: File path pattern for log collection
- **Purpose**: Collects structured JSON logs from the application
- **Relationships**: Must match logback-spring.xml file output path and docker volume mount

#### pipeline_stages.json.expressions
- **Type**: object
- **Description**: JSON field extraction configuration
- **Purpose**: Extracts fields like traceId, spanId, operation from JSON logs
- **Critical**: Essential for log correlation and filtering in Grafana

#### pipeline_stages.labels
- **Type**: object
- **Description**: Low-cardinality labels for Loki streams
- **Purpose**: Uses only level, logger, operation, service, environment to avoid stream explosion
- **Critical**: High-cardinality fields (traceId, spanId) are extracted but not used as labels

### Prometheus Configuration (prometheus.yml)

#### global.scrape_interval
- **Type**: duration
- **Current Value**: `15s`
- **Description**: Default interval for scraping metrics
- **Purpose**: Balances monitoring frequency with resource usage

#### scrape_configs[1].job_name
- **Type**: string
- **Current Value**: `digital-wallet-api`
- **Description**: Job name for wallet API metrics
- **Purpose**: Identifies metrics source in Prometheus

#### scrape_configs[1].static_configs[0].targets
- **Type**: array
- **Current Value**: `['host.docker.internal:8080']`
- **Description**: Target endpoints for metrics collection
- **Purpose**: Scrapes metrics from /actuator/prometheus endpoint
- **Relationships**: Must match application server port

---

## Configuration Dependencies

### Critical Relationships

1. **Database Connection Chain**:
   - spring.datasource.* → PostgreSQL connection
   - spring.flyway.* → Schema management
   - spring.jpa.hibernate.ddl-auto=validate → Schema validation

2. **Tracing Chain**:
   - otel.* → OpenTelemetry configuration
   - logging.pattern.console → Includes traceId/spanId
   - TraceContextFilter → Populates MDC
   - Promtail → Extracts traceId/spanId from logs

3. **Monitoring Chain**:
   - management.endpoints.* → Exposes endpoints
   - Prometheus scrape_configs → Collects metrics
   - Grafana datasources → Visualizes data

4. **Log Processing Chain**:
   - logback-spring.xml → Generates JSON logs
   - Docker volume mount → Makes logs accessible
   - Promtail scrape_configs → Collects logs
   - Loki limits_config → Prevents ingestion errors

### Port Dependencies

- **8080**: Application server (default, commented in config)
- **3100**: Loki HTTP API
- **9080**: Promtail HTTP server
- **9090**: Prometheus web UI
- **3000**: Grafana web UI
- **5432**: PostgreSQL database

---

## Environment-Specific Settings

### Development Environment
- **Logging**: DEBUG level, detailed SQL logging
- **Security**: Hardcoded JWT secret
- **Database**: Local PostgreSQL
- **Monitoring**: All endpoints exposed
- **Loki**: No authentication, local storage

### Production Recommendations
- **Logging**: INFO level, no SQL logging
- **Security**: External secret management
- **Database**: External PostgreSQL with connection pooling
- **Monitoring**: Restricted endpoint exposure
- **Loki**: Authentication enabled, external storage

This configuration reference ensures all properties are documented and their relationships understood for proper system operation and maintenance.

---

## 🌍 Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [Referência de Configuração em Português](../pt/referencia-configuracao.md)

---

*For more information, see the [main project documentation](../../../README.md).*
