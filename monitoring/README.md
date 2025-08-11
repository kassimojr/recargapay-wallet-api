# Monitoring Configuration

This directory contains all monitoring and observability configurations for the RecargaPay Wallet API.

## Structure

- `/prometheus`: Contains Prometheus configuration files
  - `prometheus.yml`: Main configuration file defining scrape targets and intervals

- `/grafana`: Contains Grafana provisioning files
  - `datasources.yml`: Automatic configuration for Prometheus data source
  - `dashboards.yml`: Dashboard provider configuration

- `/dashboards`: Contains Grafana dashboards
  - `wallet-metrics-dashboard.json`: Main dashboard for Wallet API metrics visualization

## Usage

The monitoring stack can be started using the included script:

```bash
./start-monitoring.sh
```

This will start PostgreSQL, Prometheus, Grafana, and SonarQube via Docker Compose.

## Available Endpoints

- Health Checks: `http://localhost:8080/actuator/health`
  - Readiness Probe: `http://localhost:8080/actuator/health/readiness`
  - Liveness Probe: `http://localhost:8080/actuator/health/liveness`
  - Wallet Service Health: `http://localhost:8080/actuator/health/wallet`

- Metrics: `http://localhost:8080/actuator/prometheus`

- Log Level Management: `http://localhost:8080/actuator/loggers`

## Monitoring Components

- **Prometheus**: `http://localhost:9090`
  - Used for metrics collection and storage
  - Scrapes the Wallet API every 5 seconds

- **Grafana**: `http://localhost:3000` (login: admin/admin)
  - Used for metrics visualization
  - Pre-configured dashboards in the RecargaPay folder

## Related Code

The implementation uses:
- Spring Boot Actuator for exposing metrics and health checks
- Micrometer with @Timed annotations for recording metrics
- Custom health indicators in `com.recargapay.wallet.infra.health`
- Distributed tracing with @Traced annotations in service classes
