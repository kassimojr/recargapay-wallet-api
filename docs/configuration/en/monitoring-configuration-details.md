# Monitoring Configuration Details

## Overview

This document provides detailed explanations for all monitoring stack configurations, including Loki, Promtail, Prometheus, Grafana, and Tempo configurations.

---

## Loki Configuration (monitoring/loki/loki-config.yaml)

### Server Configuration

#### server.http_listen_port: 3100
- **Purpose**: HTTP API port for log ingestion and queries
- **Impact**: All clients (Promtail, Grafana) must connect to this port
- **Relationships**: Referenced in docker-compose.yml port mapping (3100:3100)

### Ingester Configuration

#### ingester.lifecycler.address: 127.0.0.1
- **Purpose**: Bind address for the ingester component
- **Development**: 127.0.0.1 for local-only access
- **Production**: Should be 0.0.0.0 for cluster deployment

#### ingester.lifecycler.ring.kvstore.store: inmemory
- **Purpose**: Key-value store for ring membership
- **Development**: inmemory for simplicity
- **Production**: Should use consul or etcd for persistence

#### ingester.lifecycler.ring.replication_factor: 1
- **Purpose**: Number of replicas for each log stream
- **Development**: 1 for single-instance deployment
- **Production**: 3+ for high availability

#### ingester.chunk_idle_period: 5m
- **Purpose**: Time before flushing idle chunks to storage
- **Impact**: Shorter periods = more frequent flushes = better query performance
- **Trade-off**: More I/O operations vs. memory usage

#### ingester.chunk_retain_period: 30s
- **Purpose**: Time to retain chunks in memory after flushing
- **Impact**: Improves query performance for recent logs
- **Memory**: Higher values use more memory

### Schema Configuration

#### schema_config.configs[0].from: 2020-10-24
- **Purpose**: Start date for this schema configuration
- **Requirement**: Must be before any log entries
- **Impact**: Affects how logs are indexed and stored

#### schema_config.configs[0].store: boltdb
- **Purpose**: Index storage backend
- **Development**: boltdb for local file storage
- **Production**: cassandra or bigtable for scalability

#### schema_config.configs[0].object_store: filesystem
- **Purpose**: Chunk storage backend
- **Development**: filesystem for local storage
- **Production**: s3, gcs, or azure for scalability

#### schema_config.configs[0].schema: v11
- **Purpose**: Index schema version
- **Impact**: Affects query performance and storage efficiency
- **Requirement**: Must match Loki version capabilities

### Storage Configuration

#### storage_config.boltdb.directory: /loki/index
- **Purpose**: Local directory for BoltDB index files
- **Docker**: Mapped to loki_data volume
- **Permissions**: Must be writable by Loki process

#### storage_config.filesystem.directory: /loki/chunks
- **Purpose**: Local directory for chunk files
- **Docker**: Mapped to loki_data volume
- **Permissions**: Must be writable by Loki process

### Limits Configuration (Critical for Stability)

#### limits_config.max_streams_per_user: 10000
- **Purpose**: Prevents cardinality explosion
- **Problem Solved**: "Maximum active stream limit exceeded" HTTP 429 errors
- **Impact**: Essential for stable log ingestion with high-cardinality data

#### limits_config.ingestion_rate_mb: 4
- **Purpose**: Controls ingestion throughput (4 MB/s)
- **Impact**: Logs may be rejected if rate is exceeded
- **Tuning**: Increase for high-volume applications

#### limits_config.ingestion_burst_size_mb: 6
- **Purpose**: Allows temporary ingestion spikes (6 MB burst)
- **Relationship**: Should be higher than ingestion_rate_mb
- **Impact**: Handles traffic bursts without rejection

#### limits_config.per_stream_rate_limit: 3MB
- **Purpose**: Rate limit per individual stream
- **Impact**: Prevents single stream from overwhelming system
- **Complement**: Works with global rate limits

#### limits_config.max_line_size: 256000
- **Purpose**: Maximum size for individual log lines (256KB)
- **Impact**: Large log entries will be truncated or rejected
- **JSON Logs**: Usually sufficient for structured logs

#### limits_config.max_entries_limit_per_query: 5000
- **Purpose**: Limits query result size
- **Impact**: Large queries may be truncated
- **Performance**: Prevents memory exhaustion

#### limits_config.cardinality_limit: 100000
- **Purpose**: Maximum unique label combinations
- **Impact**: Prevents label cardinality explosion
- **Critical**: Essential for performance with many unique labels

---

## Promtail Configuration (monitoring/promtail/promtail-config.yaml)

### Server Configuration

#### server.http_listen_port: 9080
- **Purpose**: HTTP server port for Promtail metrics and status
- **Conflict Avoidance**: Uses 9080 to avoid conflicts with application (8080)

#### server.grpc_listen_port: 0
- **Purpose**: Disables gRPC server
- **Simplification**: Only HTTP needed for this deployment

### Position Tracking

#### positions.filename: /tmp/positions.yaml
- **Purpose**: Tracks reading position in log files
- **Restart Behavior**: Prevents re-reading logs after restart
- **Location**: Temporary directory suitable for development

### Client Configuration

#### clients[0].url: http://loki:3100/loki/api/v1/push
- **Purpose**: Loki endpoint for log shipping
- **Docker**: Uses service name 'loki' from docker-compose.yml
- **Port**: Must match Loki's http_listen_port

### Scrape Configuration

#### scrape_configs[0].job_name: wallet-api-json-logs
- **Purpose**: Identifies this log source in Loki
- **Labeling**: Appears as job="wallet-api" in logs

#### static_configs[0].labels.__path__: /app/logs/wallet-api*.json
- **Purpose**: File pattern for log collection
- **Docker Volume**: /app/logs mapped to ./logs in docker-compose.yml
- **Pattern**: Matches wallet-api.json and rotated files

### Pipeline Stages (Critical for Log Processing)

#### json.expressions
- **Purpose**: Extracts fields from JSON log entries
- **Fields Extracted**:
  - timestamp: For proper log ordering
  - level: Log level (INFO, DEBUG, ERROR)
  - message: Log message content
  - logger: Java class name
  - traceId: Distributed tracing identifier
  - spanId: Span identifier
  - operation: Business operation (DEPOSIT, WITHDRAW, etc.)
  - walletId: Wallet identifier
  - amount: Transaction amount

#### timestamp.source: timestamp
- **Purpose**: Uses extracted timestamp for log ordering
- **Format**: RFC3339Nano format from logback
- **Critical**: Ensures correct chronological ordering

#### labels (Low Cardinality Only)
- **Included**: level, logger, operation, service, environment
- **Excluded**: traceId, spanId, walletId (high cardinality)
- **Purpose**: Prevents stream explosion while maintaining queryability
- **Critical**: High-cardinality fields cause "too many streams" errors

---

## Prometheus Configuration (monitoring/prometheus/prometheus.yml)

### Global Configuration

#### global.scrape_interval: 15s
- **Purpose**: Default interval for metrics collection
- **Balance**: Frequency vs. resource usage
- **Override**: Individual jobs can specify different intervals

#### global.evaluation_interval: 15s
- **Purpose**: How often to evaluate alerting rules
- **Consistency**: Matches scrape interval for rule evaluation

### Scrape Configurations

#### Job: prometheus (Self-monitoring)
- **Target**: localhost:9090
- **Purpose**: Prometheus monitors itself
- **Metrics**: Prometheus internal metrics

#### Job: recargapay-wallet-api
- **Target**: host.docker.internal:8080
- **Metrics Path**: /actuator/prometheus
- **Scrape Interval**: 5s (more frequent than default)
- **Purpose**: Collects application metrics
- **Docker**: host.docker.internal resolves to host machine

#### Job: docker
- **Target**: host.docker.internal:9323
- **Purpose**: Docker daemon metrics (if enabled)
- **Optional**: May not be available in all environments

---

## Grafana Configuration

### Datasources (monitoring/grafana/datasources.yml)

#### Prometheus Datasource
- **URL**: http://prometheus:9090
- **Purpose**: Metrics visualization
- **Docker**: Uses service name from docker-compose.yml

#### Loki Datasource
- **URL**: http://loki:3100
- **Purpose**: Log visualization and correlation
- **Docker**: Uses service name from docker-compose.yml

#### Tempo Datasource
- **URL**: http://tempo:3200
- **Purpose**: Distributed tracing visualization
- **Docker**: Uses service name from docker-compose.yml

### Dashboards (monitoring/grafana/dashboards.yml)

#### Dashboard Provider
- **Path**: /etc/grafana/dashboards
- **Purpose**: Auto-loads dashboard JSON files
- **Docker Volume**: ./monitoring/dashboards mapped to container

---

## Tempo Configuration (monitoring/tempo/tempo-config.yaml)

### Server Configuration

#### server.http_listen_port: 3200
- **Purpose**: HTTP API for trace queries
- **Grafana**: Used by Grafana for trace visualization

#### server.grpc_listen_port: 9095
- **Purpose**: gRPC API for trace ingestion
- **Usage**: OTLP trace receivers

### Distributor Configuration

#### distributor.receivers.otlp.protocols.grpc.endpoint: 0.0.0.0:4317
- **Purpose**: Receives OTLP traces via gRPC
- **Standard**: OpenTelemetry standard port
- **Docker**: Exposed as 4317:4317

#### distributor.receivers.otlp.protocols.http.endpoint: 0.0.0.0:4318
- **Purpose**: Receives OTLP traces via HTTP
- **Alternative**: HTTP alternative to gRPC

### Storage Configuration

#### storage.trace.backend: local
- **Purpose**: Local file storage for traces
- **Development**: Suitable for development
- **Production**: Should use cloud storage (s3, gcs)

#### storage.trace.local.path: /var/tempo
- **Purpose**: Local storage directory
- **Docker**: Mapped to tempo_data volume

---

## Configuration Relationships

### Docker Compose Integration

1. **Service Names**: Used in URLs (loki:3100, prometheus:9090)
2. **Port Mappings**: External access to services
3. **Volume Mounts**: Persistent storage and config files
4. **Dependencies**: Service startup order

### Log Flow

1. **Application** → JSON logs → **File System**
2. **Promtail** → Scrapes files → **Extracts JSON**
3. **Promtail** → Ships logs → **Loki**
4. **Grafana** → Queries → **Loki** → Displays logs

### Metrics Flow

1. **Application** → Exposes metrics → **/actuator/prometheus**
2. **Prometheus** → Scrapes → **Application**
3. **Grafana** → Queries → **Prometheus** → Displays metrics

### Trace Flow

1. **Application** → Generates traces → **TraceContextFilter**
2. **Logs** → Include traceId/spanId → **Correlation**
3. **Grafana** → Correlates → **Logs by traceId**

---

## Troubleshooting Common Issues

### "Maximum active stream limit exceeded"
- **Cause**: High-cardinality labels in Promtail
- **Solution**: Remove traceId, spanId from labels section
- **Config**: Keep them in json.expressions only

### "Connection refused" errors
- **Cause**: Service not ready or wrong port
- **Check**: Docker service status and port mappings
- **Solution**: Verify service dependencies

### Missing metrics in Prometheus
- **Cause**: Wrong target or path
- **Check**: /actuator/prometheus endpoint accessibility
- **Solution**: Verify management.endpoints.web.exposure.include

### Logs not appearing in Grafana
- **Cause**: Promtail not finding files or Loki rejection
- **Check**: File paths, permissions, Loki limits
- **Solution**: Verify volume mounts and file generation

This detailed configuration reference ensures proper understanding and maintenance of the monitoring stack.

---

## 🌍 Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [Detalhes de Configuração de Monitoramento em Português](../pt/detalhes-configuracao-monitoramento.md)

---

*For more information, see the [main project documentation](../../../README.md).*
