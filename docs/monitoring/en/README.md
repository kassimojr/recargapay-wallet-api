# 📊 Monitoring & Observability

This section covers the comprehensive monitoring and observability setup for the RecargaPay Wallet API, including metrics collection, dashboards, alerting, and distributed tracing.

## 📋 Quick Navigation

| 📄 Document | 📝 Description | 🎯 Audience |
|-------------|----------------|-------------|
| [Observability Setup](monitoring-guide-en.md) | Complete observability stack configuration | DevOps, SRE |
| [Dashboard Guide](monitoring-guide-en.md#exploring-grafana-dashboards) | Grafana dashboard creation and configuration | Developers, SRE |
| [Metrics Reference](monitoring-guide-en.md#viewing-metrics) | Complete list of available metrics | Developers, SRE |
| [Alerting Guide](monitoring-guide-en.md#best-practices) | Alert configuration and notifications | DevOps, SRE |

## 🎯 Observability Stack

### Core Components
- **Grafana** - Visualization and dashboards (port 3000)
- **Loki** - Log aggregation and querying
- **Promtail** - Log collection and forwarding
- **Prometheus** - Metrics collection (via Spring Boot Actuator)
- **OpenTelemetry** - Distributed tracing integration

### Architecture Overview
```
Application → Actuator → Prometheus Metrics
     ↓
Structured Logs → Promtail → Loki → Grafana
     ↓
Tracing Context (traceId/spanId) → Correlation
```

## 🔧 Technical Implementation

### Health Checks
- **Custom Health Indicators**: WalletDatabaseHealthIndicator, WalletServiceHealthIndicator
- **Health Groups**: Readiness (`/actuator/health/readiness`) and Liveness (`/actuator/health/liveness`) probes
- **Kubernetes Integration**: Ready for container orchestration health monitoring

### Metrics Collection
- **Standard Micrometer @Timed**: API response times for all controller endpoints
- **Custom Gauges**: Real-time wallet balance tracking
- **Counters**: Transaction counts by type and error rates
- **Endpoint**: `/actuator/prometheus` for metrics exposure

### Logging Configuration
- **Structured Logging**: JSON format with timestamp, thread, level, and message
- **Trace Context**: Includes traceId and spanId for distributed correlation
- **Log Rotation**: Automatic rotation (10MB files, 10 file history)
- **Dynamic Log Levels**: Adjustable via `/actuator/loggers` endpoints

### Distributed Tracing
- **@Traced Annotation**: Built on Micrometer's @Observed API
- **Core Operations**: All service methods (deposit, withdraw, transfer) are traced
- **Trace Correlation**: Trace IDs propagated in logs for end-to-end tracking

## 🚀 Getting Started

### For Developers
1. **[Grafana Setup](monitoring-guide-en.md#accessing-the-components)** - Initial Grafana configuration
2. **[Log Queries](../../tracing/en/loki-queries-traceid.md)** - How to query structured logs
3. **[Metrics Guide](monitoring-guide-en.md#viewing-metrics)** - Understanding available metrics

### For DevOps/SRE
1. **[Complete Setup](monitoring-guide-en.md)** - Full stack configuration
2. **[Alerting Guide](monitoring-guide-en.md#best-practices)** - Configure important alerts
3. **[Dashboard Guide](monitoring-guide-en.md#exploring-grafana-dashboards)** - Create custom dashboards

## 📈 Key Metrics

### Application Metrics
- **HTTP Request Metrics**: Response times, status codes, throughput
- **Database Metrics**: Connection pool usage, query performance
- **Cache Metrics**: Hit/miss ratios, Redis performance
- **JVM Metrics**: Memory usage, garbage collection, thread pools

### Business Metrics
- **Wallet Operations**: Creation, balance updates, transaction counts
- **Financial Metrics**: Transaction volumes, success rates
- **User Activity**: API usage patterns, authentication rates

### Infrastructure Metrics
- **System Resources**: CPU, memory, disk usage
- **Network Metrics**: Connection counts, bandwidth usage
- **Container Metrics**: Docker container health and performance

## 🔍 Log Management

### Structured Logging
- **JSON format** for machine readability
- **Distributed tracing** with traceId/spanId correlation
- **Contextual information** for debugging
- **Security-aware** logging (no sensitive data)

### Log Levels by Environment
| Environment | Application | Framework | Database |
|-------------|-------------|-----------|----------|
| **Development** | DEBUG | INFO | DEBUG |
| **Test** | INFO | WARN | WARN |
| **Homologation** | INFO | WARN | ERROR |
| **Production** | ERROR | ERROR | ERROR |

## 🚨 Alerting Strategy

### Critical Alerts
- **Application down** - Service unavailable
- **Database connection failures** - Data access issues
- **High error rates** - Application errors > 5%
- **Memory/CPU exhaustion** - Resource constraints

### Warning Alerts
- **Slow response times** - Performance degradation
- **Cache miss rates** - Performance impact
- **Disk space low** - Storage concerns
- **High transaction volumes** - Capacity planning

## 🛠️ Troubleshooting

### Common Issues

#### Logs Not Appearing in Grafana
1. Check Promtail configuration and file paths
2. Verify Loki ingestion and stream limits
3. Validate log format and JSON structure
4. Check Docker volume mounts and permissions

#### Metrics Not Available
1. Verify Actuator endpoints are exposed
2. Check Spring Boot metrics configuration
3. Validate Prometheus scraping configuration
4. Ensure proper network connectivity

#### Dashboard Issues
1. Check Grafana data source configuration
2. Verify query syntax and time ranges
3. Validate metric names and labels
4. Check dashboard permissions and access

## 🔗 Related Documentation

- **🏠 Main Documentation**: [Project README](../../../README.md)
- **⚙️ Configuration**: [Environment Setup](../../configuration/en/environment-setup.md)
- **🔍 Tracing**: [Distributed Tracing](../../tracing/en/)
- **🔒 Security**: [Security Monitoring](../../security/en/security-config.md#security-monitoring)

## 🎯 Best Practices

### Development
- Use structured logging consistently
- Include relevant context in log messages
- Monitor application metrics during development
- Test observability features locally

### Production
- Set up comprehensive alerting
- Monitor business metrics alongside technical metrics
- Regular review of dashboard effectiveness
- Capacity planning based on metrics trends

### Team Practices
- Define SLIs/SLOs for critical services
- Regular observability reviews and improvements
- Incident response procedures using observability data
- Knowledge sharing on monitoring best practices

---

## 🌍 Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [README em Português](../pt/README.md)

---

*For more information, see the [main project documentation](../../../README.md).*
