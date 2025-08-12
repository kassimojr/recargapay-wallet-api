# Configuration Documentation

## Overview

This directory contains comprehensive documentation for all configuration files and properties used in the RecargaPay Wallet API project. Each configuration is thoroughly documented with its purpose, impact, and relationships.

## Documentation Structure

### Main Documentation (English)

- **[Configuration Reference](configuration-reference.md)** - Complete reference for all configuration properties
- **[Monitoring Configuration Details](monitoring-configuration-details.md)** - Detailed monitoring stack configurations
- **[Configuration Examples](configuration-examples.md)** - Practical examples and scenarios
- **[Environment Setup Guide](environment-setup.md)** - Comprehensive documentation for all configuration aspects

### Portuguese Documentation

- **[Referência de Configuração](../pt/referencia-configuracao.md)** - Versão em português da referência completa
- **[Detalhes de Configuração de Monitoramento](../pt/detalhes-configuracao-monitoramento.md)** - Configurações detalhadas do stack de monitoramento
- **[Exemplos de Configuração](../pt/exemplos-configuracao.md)** - Exemplos práticos e cenários
- **[README em Português](../../README-PT.md)** - Índice completo em português

## Quick Reference

### Application Configuration Files

| File | Purpose | Environment |
|------|---------|-------------|
| `src/main/resources/application.yml` | Main application configuration | All |
| `src/test/resources/application-test.yml` | Test-specific configuration | Test only |

### Monitoring Stack Configuration Files

| File | Purpose | Service |
|------|---------|---------|
| `monitoring/loki/loki-config.yaml` | Log aggregation configuration | Loki |
| `monitoring/promtail/promtail-config.yaml` | Log collection configuration | Promtail |
| `monitoring/prometheus/prometheus.yml` | Metrics collection configuration | Prometheus |
| `monitoring/grafana/datasources.yml` | Data source configuration | Grafana |
| `monitoring/grafana/dashboards.yml` | Dashboard provisioning | Grafana |
| `monitoring/tempo/tempo-config.yaml` | Distributed tracing configuration | Tempo |

## Key Configuration Categories

### 🔧 Core Application
- **Spring Framework**: Application name, profiles, basic setup
- **Database**: PostgreSQL connection, JPA/Hibernate settings
- **Security**: JWT configuration, authentication
- **Migration**: Flyway database migration settings

### 📊 Monitoring & Observability
- **Logging**: Structured JSON logging with trace correlation
- **Metrics**: Prometheus metrics exposure via Actuator
- **Tracing**: OpenTelemetry distributed tracing
- **Health Checks**: Application health monitoring

### 🔍 Log Management
- **Collection**: Promtail file scraping and processing
- **Storage**: Loki log aggregation and indexing
- **Visualization**: Grafana log queries and dashboards

### 📈 Metrics & Alerting
- **Collection**: Prometheus metrics scraping
- **Storage**: Time-series metrics storage
- **Visualization**: Grafana metrics dashboards

## Critical Configuration Relationships

### Database Chain
```
spring.datasource.* → PostgreSQL Connection
spring.flyway.* → Schema Management  
spring.jpa.hibernate.ddl-auto=validate → Schema Validation
```

### Logging Chain
```
Application → JSON Logs → File System
Promtail → Scrapes Files → Extracts JSON
Promtail → Ships Logs → Loki
Grafana → Queries Loki → Displays Logs
```

### Metrics Chain
```
Application → /actuator/prometheus → Metrics Endpoint
Prometheus → Scrapes Metrics → Stores Data
Grafana → Queries Prometheus → Displays Metrics
```

### Tracing Chain
```
TraceContextFilter → Generates traceId/spanId
MDC → Populates Log Context → JSON Logs
Promtail → Extracts traceId/spanId → Loki
Grafana → Correlates by traceId → Trace View
```

## Port Dependencies

| Port | Service | Purpose |
|------|---------|---------|
| 8080 | Wallet API | Main application server |
| 3100 | Loki | Log ingestion and queries |
| 9080 | Promtail | Log collection metrics |
| 9090 | Prometheus | Metrics collection and UI |
| 3000 | Grafana | Visualization dashboards |
| 5432 | PostgreSQL | Database server |

## Environment-Specific Notes

### Development
- **Logging**: DEBUG level enabled
- **Security**: Hardcoded secrets (acceptable)
- **Database**: Local PostgreSQL
- **Monitoring**: All endpoints exposed
- **Storage**: Local file storage

### Production Recommendations
- **Logging**: INFO level, structured format
- **Security**: External secret management
- **Database**: External PostgreSQL with pooling
- **Monitoring**: Restricted endpoint exposure
- **Storage**: Cloud storage backends

## Common Configuration Patterns

### High Availability
- Multiple Loki instances with shared storage
- Prometheus federation for scalability
- Load-balanced application instances

### Security Hardening
- JWT with external key management
- Database connection encryption
- Restricted actuator endpoints
- Authentication for monitoring services

### Performance Optimization
- Connection pool tuning
- Log retention policies
- Metrics cardinality management
- Query performance optimization

## Troubleshooting Quick Reference

### "Maximum active stream limit exceeded"
- **Cause**: High-cardinality labels in Promtail
- **Fix**: Remove traceId/spanId from labels section
- **Config**: Keep in json.expressions only

### Connection Refused Errors
- **Cause**: Service not ready or wrong port
- **Check**: Docker service status and port mappings
- **Fix**: Verify service dependencies in docker-compose.yml

### Missing Metrics
- **Cause**: Wrong Prometheus target or path
- **Check**: /actuator/prometheus endpoint accessibility
- **Fix**: Verify management.endpoints.web.exposure.include

### Logs Not Appearing
- **Cause**: Promtail file access or Loki rejection
- **Check**: File paths, permissions, Loki limits
- **Fix**: Verify volume mounts and log generation

## Best Practices

### Configuration Management
1. **Environment Variables**: Use for sensitive data in production
2. **Validation**: Always validate configurations before deployment
3. **Documentation**: Keep this documentation updated with changes
4. **Testing**: Test configuration changes in development first

### Security
1. **Secrets**: Never commit secrets to version control
2. **Access**: Restrict monitoring endpoint access in production
3. **Encryption**: Use TLS for all external communications
4. **Auditing**: Log configuration changes

### Performance
1. **Monitoring**: Monitor configuration impact on performance
2. **Tuning**: Regularly review and tune based on usage patterns
3. **Scaling**: Plan for horizontal scaling requirements
4. **Optimization**: Optimize based on actual usage metrics

## Configuration & Environment Setup

### ⚙️ Configuration & Environment Setup

This section contains comprehensive documentation for all configuration aspects of the RecargaPay Wallet API, including environment setup, local development, and deployment configurations.

### 📋 Quick Navigation

| 📄 Document | 📝 Description | 🎯 Audience |
|-------------|----------------|-------------|
| [Local Development Setup](local-setup.md) | Quick setup guide for local development | New Developers |
| [Environment Setup Guide](environment-setup.md) | Complete environment configuration for all profiles | Developers, DevOps |

### 🚀 Getting Started

#### For New Developers
1. **Start here**: [Local Development Setup](local-setup.md)
2. **Complete setup**: [Environment Setup Guide](environment-setup.md)
3. **Join the team**: [Team Onboarding](../../onboarding/en/team-onboarding.md)

#### For DevOps/SysAdmin
1. **Environment guide**: [Complete Environment Setup](environment-setup.md)
2. **Security setup**: [Security Configuration](../../security/en/security-config.md)

### 🌍 Environment Profiles

The application supports multiple environment profiles with specific configurations:

| Environment | Profile | Configuration File | Security Level |
|-------------|---------|-------------------|----------------|
| **Development** | `dev` | `application-dev.yml` | Low (Full debugging) |
| **Test** | `test` | `application-test.yml` | Low (Testing focused) |
| **Homologation** | `hml` | `application-hml.yml` | Medium (Pre-production) |
| **Production** | `prod` | `application-prod.yml` | High (Maximum security) |

### 🔧 Configuration Categories

#### Database Configuration
- **PostgreSQL** for dev/hml/prod environments
- **H2 in-memory** for test environment
- **Connection pooling** and performance tuning
- **Flyway migrations** for schema management

#### Cache Configuration
- **Redis distributed cache** with configurable TTLs
- **Environment-specific** cache settings
- **Performance optimization** for financial data
- **Cache versioning** and invalidation strategies

#### Security Configuration
- **JWT authentication** with environment-based secrets
- **Environment variable validation** at startup
- **Actuator endpoint protection** by environment
- **Security headers** and CORS configuration

#### Logging Configuration
- **Structured JSON logging** with tracing support
- **Environment-specific log levels**
- **Distributed tracing** with traceId/spanId
- **Log aggregation** with Loki/Grafana

#### Monitoring Configuration
- **Health checks** for all dependencies
- **Metrics collection** and exposure
- **Observability stack** integration
- **Environment-appropriate** endpoint exposure

This documentation ensures comprehensive understanding of all configuration aspects for effective system operation and maintenance.

---

## 🌍 Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [README em Português](../pt/README.md)

---

*For more information, see the [main project documentation](../../../README.md).*
