# Digital Wallet API Monitoring Guide

## Introduction to Monitoring and Observability

Modern software systems require comprehensive monitoring to ensure reliability, availability, and performance. This guide explains how to use the monitoring stack implemented for the Digital Wallet API.

### Core Concepts

**Monitoring** involves collecting, processing, and displaying metrics about your system to understand its health and performance. 

**Observability** extends monitoring by providing deeper insights into what's happening inside the system, typically through three pillars:
1. **Metrics**: Numerical data about system performance
2. **Logging**: Detailed text records of events
3. **Tracing**: Tracking the flow of requests through distributed systems

## Monitoring Stack Components

The Digital Wallet API monitoring solution is built with industry-standard tools:

### 1. Spring Boot Actuator
Provides endpoints to expose application health, metrics, and operational information.

### 2. Micrometer
A metrics instrumentation library that collects and distributes metrics data.

### 3. Prometheus
An open-source monitoring and alerting toolkit that scrapes and stores metrics.

### 4. Grafana
A visualization platform that displays metrics in customizable dashboards.

## Setup and Launch

### Starting the Monitoring Environment

1. Navigate to the project root:
   ```bash
   cd /path/to/digital-wallet-api
   ```

2. Run the monitoring setup script:
   ```bash
   ./monitoring.sh
   ```

3. Wait for all services to start (PostgreSQL, Prometheus, Grafana, etc.)

### Accessing the Components

- **Wallet API**: http://localhost:8080
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (use admin/admin as credentials)
- **SonarQube**: http://localhost:9000

## Using the Monitoring Features

### Health Checks

Health checks provide information about the API's operational status:

1. Access the general health status:
   ```
   http://localhost:8080/actuator/health
   ```

2. Access specific health checks:
   - Readiness probe: `http://localhost:8080/actuator/health/readiness`
   - Liveness probe: `http://localhost:8080/actuator/health/liveness`
   - Wallet service health: `http://localhost:8080/actuator/health/wallet`

A typical health check response looks like:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "walletService": {
      "status": "UP"
    }
  }
}
```

### Viewing Metrics

Raw metrics are available in Prometheus format at:
```
http://localhost:8080/actuator/prometheus
```

Key metrics include:
- `wallet_balance_total`: Current wallet balances
- `wallet_transaction_count`: Number of transactions by type
- `http_server_requests_seconds`: API response times
- `jvm_memory_used_bytes`: Memory usage

### Using Prometheus

1. Access Prometheus at http://localhost:9090
2. In the query field, enter metric names like `wallet_balance_total`
3. Click "Execute" to see the result
4. Use the graph tab to visualize metrics over time

Advanced queries:
- `rate(http_server_requests_seconds_count{uri="/api/v1/wallet"}[5m])`: Request rate over 5 minutes
- `histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le))`: 95th percentile response time

### Exploring Grafana Dashboards

1. Access Grafana at http://localhost:3000 (login with admin/admin)
2. Navigate to Dashboards → Digital → Wallet API Monitoring

The dashboard contains:
- **Overview panels**: Current total balance and transaction counts
- **Performance metrics**: API response times and error rates
- **Transaction panels**: Breakdown of transaction types and amounts
- **System health**: Server resource usage and JVM metrics

To customize the dashboard:
1. Click the gear icon at the top
2. Select "Edit" 
3. Modify panels as needed
4. Save your changes

### Adjusting Log Levels

Runtime log level adjustment is available at:
```
http://localhost:8080/actuator/loggers
```

To change a log level:
1. Send a POST request with the desired level:
   ```bash
   curl -X POST http://localhost:8080/actuator/loggers/com.digital.wallet \
     -H 'Content-Type: application/json' \
     -d '{"configuredLevel": "DEBUG"}'
   ```
2. Available levels: TRACE, DEBUG, INFO, WARN, ERROR

## Testing the Monitoring System

### Generate Sample Data

Use these commands to generate metrics:

1. Create a wallet:
   ```bash
   curl -X POST http://localhost:8080/api/v1/wallet \
     -H 'Content-Type: application/json' \
     -d '{"userId":"user123","initialBalance":100.00}'
   ```
   Note the returned wallet ID for subsequent commands.

2. Deposit funds:
   ```bash
   curl -X POST http://localhost:8080/api/v1/wallet/{WALLET_ID}/deposit \
     -H 'Content-Type: application/json' \
     -d '{"amount":50.00}'
   ```

3. Withdraw funds:
   ```bash
   curl -X POST http://localhost:8080/api/v1/wallet/{WALLET_ID}/withdraw \
     -H 'Content-Type: application/json' \
     -d '{"amount":25.00}'
   ```

### Verifying Metrics Collection

1. After generating transactions, check the Grafana dashboard
2. Look for changes in:
   - Wallet balance
   - Transaction counts
   - API response time graphs

## Understanding the Implementation

### Metrics Implementation

Key classes:
- `MetricsConfig`: Configures metric collection
- `@Timed` annotations: Added to controller methods to track response times
- Service methods with metrics for transaction details

### Health Check Implementation

Custom health indicators:
- `WalletDatabaseHealthIndicator`: Checks database connectivity
- `WalletServiceHealthIndicator`: Validates wallet service functionality

### Distributed Tracing

Implementation details:
- `TracingConfig`: Configures the Micrometer Observation API
- `@Traced` annotation: Applied to service methods for tracing
- Enhanced logging with traceId and spanId for correlation

## Best Practices

1. **Regular Monitoring**: Check dashboards regularly, not just during incidents
2. **Baseline Establishment**: Learn what "normal" looks like for your metrics
3. **Dashboard Organization**: Group related metrics for easier analysis
4. **Alert Configuration**: Set up alerts for critical thresholds
5. **Logging Guidelines**: Maintain consistent logging levels and formats
6. **Performance Measurement**: Use metrics to drive performance improvements

## Troubleshooting

### Common Issues

1. **Services not starting**: Check Docker logs with `docker logs wallet-prometheus`
2. **Missing metrics**: Verify that Prometheus targets are up in the Prometheus UI
3. **Dashboard not showing data**: Check Grafana data source configuration

### Quick Fixes

1. Restart the stack: `docker-compose down && ./start-monitoring.sh`
2. Check service health: `docker-compose ps`
3. Verify network connectivity: `curl http://localhost:8080/actuator/health`

## Conclusion

This monitoring setup provides comprehensive visibility into the Digital Wallet API. By regularly reviewing metrics and health checks, you can ensure optimal performance and quickly identify issues before they affect users. The combination of metrics, health checks, and distributed tracing creates a robust observability solution for the entire application.

---

## 🌍 Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [Guia de Monitoramento em Português](../pt/guia-monitoramento-pt.md)

---

*For more information, see the [main project documentation](../../../README.md).*
