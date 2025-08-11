# Distributed Tracing & Log Correlation

This section contains comprehensive documentation about the distributed tracing implementation in the RecargaPay Wallet API, including structured logging, correlation, and observability features.

## Quick Navigation

| Document | Description | Audience |
|-------------|----------------|-------------|
| [Current Implementation](current-implementation.md) | Current tracing implementation details | Developers, SRE |
| [Distributed Tracing Upgrade](distributed-tracing-upgrade.md) | Complete upgrade plan for granular tracing | Architects, DevOps |
| [Troubleshooting Guide](troubleshooting.md) | Common issues and solutions | Developers, SRE |
| [Queries & Monitoring](queries-and-monitoring.md) | Loki queries and monitoring examples | Developers, Ops |
| [Improved Logging Features](improved-logging-features.md) | Enhanced business operation logging with operation fields visible in Grafana | Developers, Ops |

## Tracing Overview

### Current Implementation
- **Basic correlation**: Single span per HTTP request
- **Unique traceId/spanId**: Generated for each request
- **MDC integration**: Available in all structured logs
- **No external dependencies**: Works without OpenTelemetry collector
- **JSON structured logs**: Machine-readable format with context

### Correlation Capabilities
- **Request tracking**: Follow complete request lifecycle
- **Error correlation**: Link errors to specific requests
- **Performance analysis**: Identify slow requests and operations
- **Business flow tracking**: Trace financial operations end-to-end

## Getting Started

### For Developers
1. **Understand current tracing**: [Current Implementation](current-implementation.md)
2. **Learn log correlation**: [Queries & Monitoring](queries-and-monitoring.md)
3. **Debug issues**: [Troubleshooting Guide](troubleshooting.md)
4. **Explore improved logging**: [Improved Logging Features](improved-logging-features.md)

### For SRE/DevOps
1. **Monitor distributed flows**: [Queries & Monitoring](queries-and-monitoring.md)
2. **Plan improvements**: [Distributed Tracing Upgrade](distributed-tracing-upgrade.md)
3. **Resolve issues**: [Troubleshooting Guide](troubleshooting.md)
4. **Utilize enhanced logging**: [Improved Logging Features](improved-logging-features.md)

### For Architects
1. **Review current architecture**: [Current Implementation](current-implementation.md)
2. **Plan granular tracing**: [Distributed Tracing Upgrade](distributed-tracing-upgrade.md)
3. **Understand trade-offs**: 
4. **Evaluate improved logging**: [Improved Logging Features](improved-logging-features.md)

## Architecture Integration

### Structured Logging
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

### Correlation Flow
```
HTTP Request → TraceContextFilter → Generate traceId/spanId
     ↓
Business Logic → DomainLogger → Structured logs with context
     ↓
Log Aggregation → Loki → Grafana → Correlation queries
```

## Key Benefits

### Operational Benefits
- **Faster debugging**: Correlate logs across request lifecycle
- **Performance insights**: Identify bottlenecks and slow operations
- **Error tracking**: Link errors to specific business operations
- **Audit trail**: Complete transaction history with correlation

### Business Benefits
- **Financial compliance**: Audit trail for all financial operations
- **Customer support**: Quick issue resolution with request tracking
- **Performance optimization**: Data-driven performance improvements
- **Incident response**: Faster root cause analysis

## Related Documentation

- **Main Documentation**: [Project README](../../../README.md)
- **Monitoring**: [Monitoring Setup](../../monitoring/en/README.md)
- **Configuration**: [Logging Configuration](../../configuration/en/environment-setup.md#logging-configuration)
- **Security**: [Security Configuration](../../security/en/security-config.md)

---

## 🌍 Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [README em Português](../pt/README.md)

---

*For more information, see the [main project documentation](../../../README.md).*

*Last updated: 2025-07-22*
