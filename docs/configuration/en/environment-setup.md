# Environment Configuration Guide

This document provides comprehensive configuration instructions for all environments of the RecargaPay Wallet API.

## Table of Contents

- [Overview](#overview)
- [Environment Profiles](#environment-profiles)
- [Environment Variables](#environment-variables)
- [Profile-Specific Configurations](#profile-specific-configurations)
- [Database Configuration](#database-configuration)
- [Redis Cache Configuration](#redis-cache-configuration)
- [Security Configuration](#security-configuration)
- [Logging Configuration](#logging-configuration)
- [Monitoring & Actuator](#monitoring--actuator)
- [Deployment Guidelines](#deployment-guidelines)
- [Troubleshooting](#troubleshooting)
- [OpenTelemetry & Distributed Tracing](#opentelemetry--distributed-tracing)

## Overview

The RecargaPay Wallet API supports multiple environments with specific configurations:

- **Development** (`dev`) - Local development with full debugging
- **Test** (`test`) - Automated testing with in-memory database
- **Homologation** (`hml`) - Pre-production testing environment
- **Production** (`prod`) - Live production environment

## Environment Profiles

### Available Profiles

| Profile | File | Purpose | Security Level |
|---------|------|---------|----------------|
| `dev` | `application-dev.yml` | Local development | Low |
| `test` | `application-test.yml` | Unit/Integration tests | Low |
| `hml` | `application-hml.yml` | Pre-production testing | Medium |
| `prod` | `application-prod.yml` | Production deployment | High |

### Profile Activation

```bash
# Via environment variable
export SPRING_PROFILES_ACTIVE=dev

# Via JVM argument
java -Dspring.profiles.active=prod -jar wallet-api.jar

# Via application.properties
spring.profiles.active=hml
```

## Environment Variables

### Automatic Environment Setup

The `.env` file is **automatically generated** when you run the startup script:

```bash
./wallet-api-startup.sh
```

The script automatically:
1. Checks if `.env` exists and is valid
2. If not, generates it from `src/main/resources/templates/.env.template`
3. Applies secure default values for development
4. Creates a backup if an existing `.env` is found

### Generated Environment Variables

The automatically generated `.env` file includes:

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=walletdb
DB_USERNAME=admin
DB_PASSWORD=admin

# JWT Security Configuration
JWT_SECRET=my-super-secure-jwt-secret-key-for-development-at-least-32-characters-long

# Redis Cache Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Cache Configuration
APP_CACHE_VERSION=v1

# Cache TTL Configuration (Financial Industry Standards)
CACHE_TTL_DEFAULT_MINUTES=2
CACHE_TTL_WALLET_LIST_MINUTES=3
CACHE_TTL_WALLET_SINGLE_MINUTES=1
CACHE_TTL_WALLET_BALANCE_SECONDS=30
CACHE_TTL_WALLET_TRANSACTIONS_MINUTES=10
CACHE_TTL_USER_PROFILE_MINUTES=15

# Application User Configuration
USER_NAME=admin
USER_PASSWORD=admin

# Application Configuration
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev

# Logging Configuration
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_APP=DEBUG

# CORS Configuration
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001,http://localhost:8080
APP_CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
APP_CORS_ALLOWED_HEADERS=*
APP_CORS_ALLOW_CREDENTIALS=true
APP_CORS_MAX_AGE=3600

# SonarQube Configuration
SONAR_USER=admin
SONAR_PASS=admin
SONAR_NEW_PASS=admin123
```

### Environment-Specific Variables

#### Development
```bash
SPRING_PROFILES_ACTIVE=dev
LOGGING_LEVEL_ROOT=DEBUG
LOGGING_LEVEL_APP=DEBUG
```

#### Test
```bash
SPRING_PROFILES_ACTIVE=test
LOGGING_LEVEL_ROOT=WARN
LOGGING_LEVEL_APP=INFO
```

#### Homologation
```bash
SPRING_PROFILES_ACTIVE=hml
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_APP=INFO
```

#### Production
```bash
SPRING_PROFILES_ACTIVE=prod
LOGGING_LEVEL_ROOT=WARN
LOGGING_LEVEL_APP=ERROR
```

## Profile-Specific Configurations

### Development Profile (`application-dev.yml`)

```yaml
spring:
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update
      format_sql: true

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
```

**Features:**
- Full SQL logging enabled
- Auto-schema updates
- All actuator endpoints exposed
- Detailed health information

### Test Profile (`application-test.yml`)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop

app:
  cache:
    version: v1
    ttl:
      default: 1  # Faster tests
```

**Features:**
- In-memory H2 database
- Schema recreated for each test
- Reduced cache TTLs for faster tests
- Fixed cache version

### Homologation Profile (`application-hml.yml`)

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  endpoint:
    health:
      show-details: when-authorized
```

**Features:**
- Schema validation only (no auto-updates)
- Restricted actuator endpoints
- Authorized-only health details
- Production-like cache settings

### Production Profile (`application-prod.yml`)

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        jdbc:
          batch_size: 20
        order_inserts: true

management:
  endpoints:
    web:
      exposure:
        include: health,prometheus
  endpoint:
    health:
      show-details: never
  server:
    port: 9090
```

**Features:**
- Performance-optimized Hibernate
- Minimal actuator endpoints
- No health details exposure
- Separate management port

## Database Configuration

### PostgreSQL Setup

#### Development/Homologation/Production
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
```

#### Test Environment
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password
```

### Flyway Migration

```yaml
spring:
  flyway:
    baseline-on-migrate: true
    enabled: true
    validate-on-migrate: true
    locations: classpath:db/migration
```

## Redis Cache Configuration

### Connection Settings

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
```

### Cache Configuration

```yaml
app:
  cache:
    version: ${APP_CACHE_VERSION}
    ttl:
      default: ${CACHE_TTL_DEFAULT_MINUTES}
      wallet-list: ${CACHE_TTL_WALLET_LIST_MINUTES}
      wallet-single: ${CACHE_TTL_WALLET_SINGLE_MINUTES}
      wallet-balance: ${CACHE_TTL_WALLET_BALANCE_SECONDS}
      wallet-transactions: ${CACHE_TTL_WALLET_TRANSACTIONS_MINUTES}
      user-profile: ${CACHE_TTL_USER_PROFILE_MINUTES}
```

### Cache Regions & TTLs

| Cache Region | TTL | Use Case |
|--------------|-----|----------|
| `wallet-list` | 3 minutes | Collection of wallets |
| `wallet-single` | 1 minute | Individual wallet data |
| `wallet-balance` | 30 seconds | Critical financial data |
| `wallet-transactions` | 10 minutes | Historical transaction data |
| `user-profile` | 15 minutes | User profile information |

## Security Configuration

### Security Headers

The application automatically configures comprehensive security headers:

#### CORS Configuration
```yaml
# CORS allowed origins (environment-specific)
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080
```

**Implemented Headers:**
- **Access-Control-Allow-Origin**: Configured per environment
- **Access-Control-Allow-Methods**: GET, POST, PUT, DELETE, OPTIONS
- **Access-Control-Allow-Headers**: Authorization, Content-Type, X-Requested-With
- **Access-Control-Allow-Credentials**: true

#### Security Headers
**Automatically configured via SecurityConfig:**
- **X-Frame-Options**: DENY (prevents clickjacking)
- **X-Content-Type-Options**: nosniff (prevents MIME sniffing)
- **X-XSS-Protection**: 1; mode=block (XSS protection)
- **Strict-Transport-Security**: max-age=31536000; includeSubDomains (HSTS)
- **Content-Security-Policy**: default-src 'self' (CSP protection)
- **Referrer-Policy**: strict-origin-when-cross-origin

#### Bean Validation
**Systematic validation implemented:**
- **@Valid**: Applied to all controller endpoints
- **JSR-303 Annotations**: @NotNull, @NotBlank, @Size, @Email, @Positive
- **Custom Validators**: Business rule validation
- **Error Handling**: RFC 7807 compliant error responses

#### JWT Authentication
- JWT secret management via environment variables
- Token expiration and refresh handling
- Method-level security annotations
- Actuator endpoint protection

### JWT Configuration

```yaml
# JWT secret must be at least 256 bits (32 characters)
JWT_SECRET=your_super_secure_jwt_secret_key_here_minimum_256_bits
```

### Admin User

```yaml
app:
  user:
    username: ${ADMIN_USERNAME}
    password: ${ADMIN_PASSWORD}
```

### Security Headers

The application automatically configures:
- CORS headers
- JWT authentication
- Method-level security
- Actuator endpoint protection

## Logging Configuration

### Log Levels by Environment

| Environment | Root Level | App Level | Framework Level |
|-------------|------------|-----------|-----------------|
| Development | DEBUG | DEBUG | INFO |
| Test | WARN | INFO | WARN |
| Homologation | INFO | INFO | WARN |
| Production | WARN | ERROR | ERROR |

### Structured Logging

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [${spring.application.name:-wallet-api}] [%X{traceId:-},%X{spanId:-}] [%thread] %-5level %logger{36} - %msg%n"
```

**Features:**
- JSON structured logging
- Distributed tracing support (traceId/spanId)
- Application name in logs
- Thread information

## OpenTelemetry & Distributed Tracing

**Implementation Details:**
- **Manual SDK Configuration**: Programmatic OpenTelemetry setup
- **W3C Trace Context**: Standard trace propagation (W3CTraceContextPropagator, W3CBaggagePropagator)
- **Custom Filters**: TraceContextFilter ensures traceId/spanId in all logs
- **MDC Integration**: Trace context automatically added to log MDC
- **Correlation**: Full request tracing across service boundaries

**Configuration:**
```yaml
# Tracing is automatically configured via OpenTelemetryConfig
# No additional environment variables required for basic setup
```

**Log Format with Tracing:**
```
2024-01-15 10:30:45.123 [wallet-api] [a1b2c3d4e5f6,1a2b3c4d] [http-nio-8080-exec-1] INFO  c.r.w.api.controller.WalletController - Processing deposit request
```

**Features:**
- Unique traceId per HTTP request
- Unique spanId per operation
- Automatic propagation in logs
- Correlation across distributed operations

## Monitoring & Actuator

### Actuator Endpoints by Environment

#### Development
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
```

#### Test
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

#### Homologation
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  endpoint:
    health:
      show-details: when-authorized
```

#### Production
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus
  endpoint:
    health:
      show-details: never
  server:
    port: 9090
```

### Health Checks

The application includes custom health indicators:
- Database connectivity
- Redis connectivity
- Application-specific checks

### Metrics

Available metrics:
- HTTP request metrics
- Database connection pool
- Cache hit/miss ratios
- Custom business metrics

## Deployment Guidelines

### Environment Setup Checklist

#### Pre-deployment
- [ ] Run `./wallet-api-startup.sh` (automatically generates `.env` if needed)
- [ ] Verify database connectivity
- [ ] Verify Redis connectivity
- [ ] Customize `.env` variables if needed (optional)
- [ ] Validate admin credentials

#### Development
- [ ] Set `SPRING_PROFILES_ACTIVE=dev`
- [ ] Enable debug logging
- [ ] Expose all actuator endpoints
- [ ] Use local database

#### Test
- [ ] Set `SPRING_PROFILES_ACTIVE=test`
- [ ] Use H2 in-memory database
- [ ] Reduce cache TTLs
- [ ] Enable test-specific configurations

#### Homologation
- [ ] Set `SPRING_PROFILES_ACTIVE=hml`
- [ ] Use production-like database
- [ ] Restrict actuator endpoints
- [ ] Enable authorized health details

#### Production
- [ ] Set `SPRING_PROFILES_ACTIVE=prod`
- [ ] Use production database
- [ ] Minimize actuator endpoints
- [ ] Disable health details
- [ ] Use separate management port
- [ ] Enable performance optimizations

### Docker Deployment

```dockerfile
# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:9090/actuator/health || exit 1
```

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      containers:
      - name: wallet-api
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_HOST
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: host
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 9090
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 9090
```

## Troubleshooting

### Common Issues

#### Application Won't Start

1. **Check environment variables**
   ```bash
   # Verify all required variables are set
   env | grep -E "(DB_|REDIS_|JWT_|ADMIN_|CACHE_)"
   ```

2. **Check profile activation**
   ```bash
   # Verify correct profile is active
   echo $SPRING_PROFILES_ACTIVE
   ```

3. **Check database connectivity**
   ```bash
   # Test PostgreSQL connection
   psql -h $DB_HOST -p $DB_PORT -U $DB_USERNAME -d $DB_NAME
   ```

4. **Check Redis connectivity**
   ```bash
   # Test Redis connection
   redis-cli -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_PASSWORD ping
   ```

#### Cache Issues

1. **Verify Redis connection**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. **Check cache keys**
   ```bash
   redis-cli -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_PASSWORD KEYS "*"
   ```

3. **Monitor cache operations**
   ```bash
   redis-cli -h $REDIS_HOST -p $REDIS_PORT -a $REDIS_PASSWORD MONITOR
   ```

#### Authentication Issues

1. **Verify JWT secret length**
   ```bash
   # JWT secret must be at least 32 characters
   echo -n "$JWT_SECRET" | wc -c
   ```

2. **Check admin credentials**
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"'$ADMIN_USERNAME'","password":"'$ADMIN_PASSWORD'"}'
   ```

#### Performance Issues

1. **Check database connection pool**
   ```bash
   curl http://localhost:8080/actuator/metrics/hikaricp.connections
   ```

2. **Monitor cache hit ratios**
   ```bash
   curl http://localhost:8080/actuator/metrics/cache.gets
   ```

3. **Check memory usage**
   ```bash
   curl http://localhost:8080/actuator/metrics/jvm.memory.used
   ```

### Log Analysis

#### Find Configuration Issues
```bash
# Search for configuration errors
grep -i "error\|exception\|failed" logs/wallet-api.log

# Check property resolution
grep -i "property\|placeholder" logs/wallet-api.log
```

#### Monitor Application Health
```bash
# Watch health status
watch -n 5 'curl -s http://localhost:8080/actuator/health | jq'

# Monitor specific health indicators
curl http://localhost:8080/actuator/health/db
curl http://localhost:8080/actuator/health/redis
```

## Best Practices

### Security
- Never commit `.env` files to version control
- Use strong JWT secrets (minimum 256 bits)
- Rotate credentials regularly
- Restrict actuator endpoints in production
- Use separate management ports in production

### Performance
- Use connection pooling for database and Redis
- Configure appropriate cache TTLs
- Enable Hibernate batch processing in production
- Monitor and tune JVM settings

### Monitoring
- Set up health checks for all dependencies
- Monitor cache hit ratios
- Track application metrics
- Set up alerting for critical issues

### Deployment
- Use infrastructure as code
- Implement blue-green deployments
- Test configurations in staging first
- Have rollback procedures ready

---

## 🌍 Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [Configuração de Ambiente em Português](../pt/configuracao-ambiente.md)

---

*For more information, see the [main project documentation](../../../README.md).*
