# Configuration Examples

## Overview

This document provides practical examples and common configuration scenarios for the Digital Wallet API.

---

## Environment-Specific Configurations

### Development Environment

#### application.yml (Development)
```yaml
spring:
  application:
    name: digital-wallet-api
  
  # Local PostgreSQL
  datasource:
    url: jdbc:postgresql://localhost:5432/walletdb
    username: admin
    password: admin
  
  # Development JPA settings
  jpa:
    hibernate:
      ddl-auto: validate  # Always validate, never create/update
    show-sql: true        # Show SQL for debugging
    properties:
      hibernate:
        format_sql: true  # Format SQL for readability

# Development logging
logging:
  level:
    com.digital.wallet: DEBUG  # Detailed application logs
    org.springframework.web: INFO # Reduce Spring noise

# Development monitoring
management:
  endpoint:
    health:
      show-details: always  # Show all health details
```

#### application.yml (Production)
```yaml
spring:
  application:
    name: digital-wallet-api
  
  # External PostgreSQL with connection pooling
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
  
  # Production JPA settings
  jpa:
    hibernate:
      ddl-auto: validate    # Always validate
    show-sql: false         # No SQL logging in production
    properties:
      hibernate:
        format_sql: false   # No formatting needed

# Production logging
logging:
  level:
    root: INFO
    com.digital.wallet: INFO  # Less verbose in production

# Production monitoring
management:
  endpoint:
    health:
      show-details: when-authorized  # Restrict health details
  endpoints:
    web:
      exposure:
        include: health,prometheus  # Minimal endpoint exposure
```

### Docker Environment

#### docker-compose.yml (Development)
```yaml
services:
  wallet-api:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/walletdb
    depends_on:
      - postgres
    volumes:
      - ./logs:/app/logs  # Log volume for Promtail

  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: walletdb
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin
    volumes:
      - postgres_data:/var/lib/postgresql/data
```

---

## Monitoring Stack Examples

### Loki Configuration Variants

#### Development (Single Instance)
```yaml
auth_enabled: false

server:
  http_listen_port: 3100

ingester:
  lifecycler:
    ring:
      kvstore:
        store: inmemory
      replication_factor: 1
  wal:
    enabled: false

storage_config:
  boltdb:
    directory: /loki/index
  filesystem:
    directory: /loki/chunks

limits_config:
  max_streams_per_user: 10000
  ingestion_rate_mb: 4
```

#### Production (Clustered)
```yaml
auth_enabled: true

server:
  http_listen_port: 3100

ingester:
  lifecycler:
    ring:
      kvstore:
        store: consul
        consul:
          host: consul:8500
      replication_factor: 3
  wal:
    enabled: true
    dir: /loki/wal

storage_config:
  aws:
    s3: s3://loki-chunks
    bucketnames: loki-chunks
    region: us-east-1
  boltdb_shipper:
    active_index_directory: /loki/index
    cache_location: /loki/cache
    shared_store: s3

limits_config:
  max_streams_per_user: 100000
  ingestion_rate_mb: 16
```

### Promtail Configuration Variants

#### Local File Collection
```yaml
server:
  http_listen_port: 9080

positions:
  filename: /tmp/positions.yaml

clients:
  - url: http://loki:3100/loki/api/v1/push

scrape_configs:
  - job_name: wallet-api-logs
    static_configs:
      - targets:
          - localhost
        labels:
          job: wallet-api
          __path__: /app/logs/wallet-api*.json
    
    pipeline_stages:
      - json:
          expressions:
            timestamp: timestamp
            level: level
            message: message
            traceId: traceId
            spanId: spanId
      
      - timestamp:
          source: timestamp
          format: RFC3339Nano
      
      - labels:
          level:
          service: wallet-api
```

#### Kubernetes Pod Collection
```yaml
server:
  http_listen_port: 9080

positions:
  filename: /tmp/positions.yaml

clients:
  - url: http://loki:3100/loki/api/v1/push

scrape_configs:
  - job_name: kubernetes-pods
    kubernetes_sd_configs:
      - role: pod
    
    relabel_configs:
      - source_labels: [__meta_kubernetes_pod_label_app]
        target_label: app
      - source_labels: [__meta_kubernetes_pod_name]
        target_label: pod
    
    pipeline_stages:
      - json:
          expressions:
            timestamp: timestamp
            level: level
            message: message
            traceId: traceId
```

---

## Security Configuration Examples

### JWT Configuration

#### Development (Hardcoded Secret)
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          secret: Q4!z8@pW#r2$Lm9^X7eF%uS6bT1&cV0*Y3jH
```

#### Production (External Secret)
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          secret: ${JWT_SECRET}  # From environment variable
          # OR
          jwk-set-uri: https://auth-server.com/.well-known/jwks.json
```

### Database Security

#### Development
```yaml
spring:
  datasource:
    username: admin
    password: admin
```

#### Production
```yaml
spring:
  datasource:
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    # OR using connection string with credentials
    url: ${DATABASE_URL}  # Contains credentials
```

---

## Performance Tuning Examples

### Database Connection Pooling

#### High Traffic Configuration
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

#### Low Traffic Configuration
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 30000
      idle-timeout: 300000
```

### JVM and Logging

#### Production JVM Settings
```yaml
# In docker-compose.yml or Kubernetes
environment:
  - JAVA_OPTS=-Xms512m -Xmx2g -XX:+UseG1GC
  
# In application.yml
logging:
  level:
    root: WARN
    com.digital.wallet: INFO
  pattern:
    console: "%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n"
```

---

## Troubleshooting Configurations

### Debug Mode

#### Enable Debug Logging
```yaml
logging:
  level:
    com.digital.wallet: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

#### Enable SQL Parameter Logging
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        type: trace
```

### Health Check Configuration

#### Detailed Health Checks
```yaml
management:
  endpoint:
    health:
      show-details: always
      show-components: always
  health:
    db:
      enabled: true
    diskspace:
      enabled: true
      threshold: 100MB
```

### Actuator Security

#### Secure Actuator Endpoints
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
      base-path: /management
  endpoint:
    health:
      show-details: when-authorized
  security:
    enabled: true
```

---

## Migration Scenarios

### Flyway Configuration Examples

#### Baseline Existing Database
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 0
    baseline-description: "Initial baseline"
```

#### Clean Database Setup
```yaml
spring:
  flyway:
    enabled: true
    clean-on-validation-error: false  # Never use in production
    validate-on-migrate: true
```

### Schema Validation

#### Strict Validation
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    validate-on-migrate: true
    clean-disabled: true  # Prevent accidental data loss
```

---

## Testing Configurations

### Test Profile (application-test.yml)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
  
  flyway:
    enabled: false  # Disable for in-memory testing

logging:
  level:
    com.digital.wallet: DEBUG
    org.springframework.test: DEBUG
```

### Integration Test Configuration
```yaml
spring:
  test:
    database:
      replace: none  # Use real database for integration tests
  
  datasource:
    url: jdbc:postgresql://localhost:5432/wallet_test
    username: test_user
    password: test_password
```

---

## Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [Exemplos de Configuração em Português](../pt/exemplos-configuracao.md)

---

*For more information, see the [main project documentation](../../../README.md).*
