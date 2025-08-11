# RecargaPay Wallet API - Monitoring Guide

This document outlines the monitoring approach for the RecargaPay Wallet API, including the metrics collected, dashboard setup, and alerting configuration.

## Overview

The monitoring stack consists of:

- **Spring Boot Actuator**: Exposes application metrics and health endpoints
- **Micrometer with Prometheus**: Collects and formats metrics in Prometheus format
- **Prometheus**: Time-series database for storing and querying metrics
- **Grafana**: Visualization and dashboarding for metrics and alerts
- **AlertManager**: Alert processing and notification

## Metrics Collection

### Standard Metrics

The following standard metrics are automatically collected:

- **JVM Metrics**: Memory usage, garbage collection, thread utilization
- **System Metrics**: CPU usage, load average, file descriptors
- **HTTP Metrics**: Request rates, response times, status codes
- **Database Connection Pool**: Connection usage, wait time, timeouts

### Custom Business Metrics

We've implemented custom metrics specific to the Wallet API:

- **Transaction Metrics**:
  - `wallet_transaction_count_total`: Count of transactions by type (deposit, withdrawal, transfer)
  - `wallet_transaction_amount_total`: Total amount of transactions by type
  - `wallet_transaction_duration_seconds`: Transaction processing time
  - `wallet_transaction_errors_total`: Count of transaction errors

- **Wallet Metrics**:
  - `wallet_balance`: Current balance in wallets (as a gauge)

## Metrics Implementation

### Available Metrics

The Wallet API exposes the following custom metrics:

| Metric Name | Type | Description | Tags |
|-------------|------|-------------|------|
| `wallet_transaction_count_total` | Counter | Total number of wallet transactions | `operation` (deposit, withdrawal, transfer) |
| `wallet_transaction_amount_total` | Counter | Total amount of wallet transactions | `operation`, `currency` |
| `wallet_transaction_duration_seconds` | Timer | Time taken to process transactions | `operation` |
| `wallet_transaction_errors_total` | Counter | Total number of transaction errors | `operation`, `error` |
| `wallet_balance` | Gauge | Current wallet balance | `wallet_id`, `currency` |
| `http_request_duration_seconds` | Timer | HTTP request duration | `endpoint` |

### Custom Currency Support

The metrics system now supports configuring the default currency and specifying custom currencies for transactions:

#### Configuration

Default currency can be configured in `application-monitoring.yml`:

```yaml
wallet:
  metrics:
    default-currency: BRL  # Change to your default currency
```

#### Using Custom Currencies

Service methods can specify a custom currency when recording metrics:

```java
// Recording with default currency
metricsService.recordDepositTransaction(amount, durationMs);

// Recording with custom currency
metricsService.recordDepositTransaction(amount, durationMs, "USD");
```

The metrics aspect will automatically detect currency codes in method parameters when possible.

### Centralized Metrics Constants

All metric names and tag keys are now centralized in the `MetricsConstants` class for better maintainability. When adding new metrics, please update this class to maintain consistency.

### Metrics Integration Points

Metrics are automatically collected at the following points:

1. **Service Layer**: Through AOP aspects that intercept deposit, withdrawal, and transfer operations
2. **Controller Layer**: Using the `@Timed` annotation from Micrometer directly on controller methods to measure endpoint performance
3. **Ad-hoc Recording**: By injecting the `MetricsService` and calling its methods directly

## Metric Collection Implementation

Metrics collection is implemented in multiple ways:

1. **Automatic Collection**: Spring Boot Actuator automatically collects system, JVM, and HTTP metrics

2. **Aspect-Based Collection**: `MetricsAspect` class intercepts wallet operations to record:
   - Transaction counts
   - Transaction amounts
   - Processing duration
   - Error counts

3. **Annotation-Based Collection**: `@Timed` annotation for fine-grained API endpoint timing

## Prometheus Configuration

Metrics are exposed at `/actuator/prometheus` and collected by Prometheus using ServiceMonitor resources.

Key configuration:
- 15-second scrape interval
- Retention period: 15 days
- Optimized scrape timeout: 14 seconds

## Alerts

We've configured the following alerts:

| Alert Name | Description | Threshold | Severity |
|------------|-------------|-----------|----------|
| WalletApiHighResponseTime | API response time is too high | P95 > 500ms for 5m | Warning |
| WalletApiHighErrorRate | HTTP error rate is too high | > 5% for 2m | Critical |
| WalletApiTransactionErrors | Business transaction errors | > 0.5/sec for 2m | Critical |
| WalletApiHighJvmMemoryUsage | JVM memory usage is too high | > 80% for 5m | Warning |
| WalletApiHighDbConnections | DB connection pool usage | > 75% for 2m | Warning |
| WalletApiDown | API is not responding | Down for > 1m | Critical |

Alert notifications are configured to be sent to:
- Slack channel: #wallet-api-alerts
- Email: devops@recargapay.com

## Dashboards

We've created a comprehensive Grafana dashboard for the Wallet API that includes:

1. **Transaction Overview**:
   - Transaction rates by type (deposit, withdrawal, transfer)
   - Transaction error counts
   - Transaction duration (p95, p50)

2. **API Performance**:
   - Request rate by endpoint
   - Response time distribution
   - Error rate by status code

3. **Resource Utilization**:
   - JVM memory usage
   - Database connection pool usage
   - System CPU and memory metrics

## Deployment

The monitoring stack is deployed as part of the Kubernetes infrastructure using Helm charts:

- Prometheus and Grafana are installed in the `monitoring` namespace
- ServiceMonitor and PrometheusRule resources are deployed to configure monitoring
- Access credentials are stored as Kubernetes secrets

## Setup Instructions

1. Run the installation script to deploy Prometheus and Grafana:

```bash
./kubernetes/monitoring/install-monitoring.sh
```

2. Access the Grafana dashboards using the URL and credentials provided by the installation script.

3. The wallet-api application must run with the `monitoring` Spring profile enabled to expose metrics:

```yaml
SPRING_PROFILES_ACTIVE: dev,monitoring
```

## Troubleshooting

### Common Issues

1. **Metrics not showing up in Prometheus**:
   - Verify the application is running with the `monitoring` profile
   - Check the ServiceMonitor is correctly targeting the application
   - Verify metrics endpoint is accessible: `curl http://wallet-api:8080/actuator/prometheus`

2. **Alerts not firing**:
   - Check PrometheusRule is correctly applied
   - Verify the alert conditions in Prometheus UI
   - Check AlertManager configuration

3. **Dashboard not showing data**:
   - Verify Prometheus data source is correctly configured in Grafana
   - Check for PromQL syntax errors in panel queries
   - Verify the metrics exist in Prometheus

### Logs

Relevant logs can be found in:

- Application logs: `kubectl logs -l app=wallet-api`
- Prometheus logs: `kubectl logs -n monitoring -l app=prometheus`
- Grafana logs: `kubectl logs -n monitoring -l app=grafana`

## Architecture Diagram

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Wallet API │     │  Prometheus │     │   Grafana   │
│             │────>│             │────>│             │
│/actuator/...│     │ Time-series │     │ Dashboards  │
└─────────────┘     └──────┬──────┘     └─────────────┘
                           │                    ▲
                           ▼                    │
                    ┌─────────────┐     ┌─────────────┐
                    │ AlertManager│     │  ServiceMon │
                    │             │     │ PrometheusR │
                    │Notifications│     │   Config    │
                    └─────────────┘     └─────────────┘
