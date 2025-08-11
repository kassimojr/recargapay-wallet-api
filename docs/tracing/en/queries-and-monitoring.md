# Queries and Monitoring Guide

## 📊 Loki Queries for Log Correlation

### Basic Trace Correlation

#### Find All Logs for a Specific Trace
```logql
{job="wallet-api"} |= "6ed5c3ad90a37e0437be0bf3f15cb9d6"
```

#### Find All Logs for a Specific Span
```logql
{job="wallet-api"} |= "6425741b3b8a7581"
```

#### Filter by Operation Type
```logql
{job="wallet-api"} | json | operation="DEPOSIT"
```

### Advanced Queries

#### Trace Timeline for a Request
```logql
{job="wallet-api"} | json 
| traceId="6ed5c3ad90a37e0437be0bf3f15cb9d6" 
| line_format "{{.timestamp}} [{{.level}}] {{.logger}} : {{.message}}"
```

#### Error Traces Only
```logql
{job="wallet-api"} | json 
| level="ERROR" 
| line_format "TraceId: {{.traceId}} - {{.message}}"
```

#### Performance Analysis by Endpoint
```logql
{job="wallet-api"} | json 
| operation="API_REQUEST_RECEIVED" 
| path="/api/v1/wallets/deposit"
| line_format "{{.timestamp}} - {{.traceId}} - {{.method}} {{.path}}"
```

#### Find Long-Running Transactions
```logql
{job="wallet-api"} | json 
| operation="API_REQUEST_COMPLETED" 
| duration > 1000
| line_format "Slow request: {{.traceId}} took {{.duration}}ms"
```

### Business Logic Queries

#### Deposit Operations
```logql
{job="wallet-api"} | json 
| operation="DEPOSIT" 
| line_format "{{.timestamp}} - Deposit: {{.walletId}} amount={{.amount}} trace={{.traceId}}"
```

#### Withdrawal Operations
```logql
{job="wallet-api"} | json 
| operation="WITHDRAW" 
| line_format "{{.timestamp}} - Withdraw: {{.walletId}} amount={{.amount}} trace={{.traceId}}"
```

#### Transfer Operations
```logql
{job="wallet-api"} | json 
| operation="TRANSFER" 
| line_format "{{.timestamp}} - Transfer: {{.fromWalletId}} -> {{.toWalletId}} amount={{.amount}} trace={{.traceId}}"
```

#### Failed Operations
```logql
{job="wallet-api"} | json 
| level="ERROR" 
| operation=~"DEPOSIT|WITHDRAW|TRANSFER"
| line_format "Failed {{.operation}}: {{.traceId}} - {{.message}}"
```

## 📈 Grafana Dashboard Panels

### 1. Request Volume Panel

```json
{
  "title": "Request Volume by Trace",
  "type": "stat",
  "targets": [
    {
      "expr": "count by (traceId) (count_over_time({job=\"wallet-api\"} | json [5m]))",
      "legendFormat": "Requests per Trace"
    }
  ]
}
```

### 2. Trace Distribution Panel

```json
{
  "title": "Trace Distribution",
  "type": "piechart",
  "targets": [
    {
      "expr": "count by (operation) (count_over_time({job=\"wallet-api\"} | json | operation!=\"\" [1h]))",
      "legendFormat": "{{operation}}"
    }
  ]
}
```

### 3. Error Rate by Trace Panel

```json
{
  "title": "Error Rate by Trace",
  "type": "timeseries",
  "targets": [
    {
      "expr": "rate(count_over_time({job=\"wallet-api\"} | json | level=\"ERROR\" [5m]))",
      "legendFormat": "Error Rate"
    }
  ]
}
```

### 4. Top Traces by Volume Panel

```json
{
  "title": "Top Traces by Log Volume",
  "type": "table",
  "targets": [
    {
      "expr": "topk(10, count by (traceId) (count_over_time({job=\"wallet-api\"} | json [1h])))",
      "format": "table"
    }
  ]
}
```

## 🔍 Debugging Workflows

### 1. Investigate Failed Request

**Step 1: Find the error**
```logql
{job="wallet-api"} | json | level="ERROR" | line_format "{{.timestamp}} {{.traceId}} {{.message}}"
```

**Step 2: Get full trace context**
```logql
{job="wallet-api"} | json | traceId="<error_trace_id>" | line_format "{{.timestamp}} [{{.level}}] {{.logger}} : {{.message}}"
```

**Step 3: Analyze request flow**
```logql
{job="wallet-api"} | json 
| traceId="<error_trace_id>" 
| operation=~"API_REQUEST_RECEIVED|API_REQUEST_COMPLETED"
| line_format "{{.timestamp}} {{.operation}} {{.method}} {{.path}} {{.duration}}ms"
```

### 2. Performance Investigation

**Step 1: Find slow requests**
```logql
{job="wallet-api"} | json 
| operation="API_REQUEST_COMPLETED" 
| duration > 2000
| line_format "Slow: {{.traceId}} {{.path}} took {{.duration}}ms"
```

**Step 2: Analyze slow trace**
```logql
{job="wallet-api"} | json 
| traceId="<slow_trace_id>" 
| line_format "{{.timestamp}} [{{.level}}] {{.logger}} : {{.message}}"
```

**Step 3: Identify bottleneck**
```logql
{job="wallet-api"} | json 
| traceId="<slow_trace_id>" 
| logger=~".*Repository.*|.*Service.*|.*Controller.*"
| line_format "{{.timestamp}} {{.logger}} : {{.message}}"
```

### 3. Business Logic Analysis

**Step 1: Find specific wallet operations**
```logql
{job="wallet-api"} | json 
| walletId="12345678-1234-1234-1234-123456789012"
| line_format "{{.timestamp}} {{.operation}} {{.amount}} trace={{.traceId}}"
```

**Step 2: Trace wallet balance changes**
```logql
{job="wallet-api"} | json 
| walletId="12345678-1234-1234-1234-123456789012" 
| operation=~"DEPOSIT|WITHDRAW|TRANSFER"
| line_format "{{.timestamp}} {{.operation}} amount={{.amount}} balance={{.newBalance}}"
```

**Step 3: Correlate with user actions**
```logql
{job="wallet-api"} | json 
| userId="user123" 
| line_format "{{.timestamp}} {{.operation}} wallet={{.walletId}} trace={{.traceId}}"
```

## 📊 Monitoring Alerts

### 1. Loki-based Alerts

#### High Error Rate Alert
```yaml
- alert: HighErrorRate
  expr: |
    (
      sum(rate(loki_request_duration_seconds_count{status_code=~"5.."}[5m])) 
      / 
      sum(rate(loki_request_duration_seconds_count[5m]))
    ) > 0.05
  for: 2m
  labels:
    severity: warning
  annotations:
    summary: "High error rate detected in wallet API"
    description: "Error rate is {{ $value | humanizePercentage }}"
```

#### Missing Trace IDs Alert
```yaml
- alert: MissingTraceIds
  expr: |
    sum(rate(count_over_time({job="wallet-api"} | json | traceId="" [5m]))) > 0
  for: 1m
  labels:
    severity: critical
  annotations:
    summary: "Trace IDs are missing from logs"
    description: "{{ $value }} logs per second are missing trace IDs"
```

#### Slow Requests Alert
```yaml
- alert: SlowRequests
  expr: |
    sum(rate(count_over_time({job="wallet-api"} | json | operation="API_REQUEST_COMPLETED" | duration > 5000 [5m]))) > 0.1
  for: 2m
  labels:
    severity: warning
  annotations:
    summary: "Slow requests detected"
    description: "{{ $value }} slow requests per second (>5s)"
```

### 2. Prometheus Metrics

#### Custom Metrics for Tracing
```java
// In your application code
@Component
public class TracingMetrics {
    
    private final Counter tracesGenerated = Counter.builder("traces_generated_total")
        .description("Total number of traces generated")
        .register(meterRegistry);
    
    private final Counter spansCreated = Counter.builder("spans_created_total")
        .description("Total number of spans created")
        .register(meterRegistry);
    
    private final Timer traceProcessingTime = Timer.builder("trace_processing_duration")
        .description("Time spent processing traces")
        .register(meterRegistry);
}
```

#### Prometheus Queries
```promql
# Trace generation rate
rate(traces_generated_total[5m])

# Span creation rate
rate(spans_created_total[5m])

# Average trace processing time
rate(trace_processing_duration_sum[5m]) / rate(trace_processing_duration_count[5m])
```

## 🎯 Use Case Examples

### 1. Customer Support Scenario

**Customer reports**: "My deposit didn't work at 10:30 AM"

**Investigation steps**:
```logql
# Step 1: Find deposits around that time
{job="wallet-api"} | json 
| operation="DEPOSIT" 
| timestamp >= "2025-07-22T10:25:00Z" 
| timestamp <= "2025-07-22T10:35:00Z"
| line_format "{{.timestamp}} {{.walletId}} {{.amount}} {{.traceId}}"

# Step 2: Check for errors in that timeframe
{job="wallet-api"} | json 
| level="ERROR" 
| timestamp >= "2025-07-22T10:25:00Z" 
| timestamp <= "2025-07-22T10:35:00Z"
| line_format "{{.timestamp}} {{.traceId}} {{.message}}"

# Step 3: Full trace analysis for suspected transaction
{job="wallet-api"} | json 
| traceId="<suspected_trace_id>" 
| line_format "{{.timestamp}} [{{.level}}] {{.logger}} : {{.message}}"
```

### 2. Performance Optimization

**Goal**: Identify slowest endpoints

**Analysis queries**:
```logql
# Step 1: Find slowest endpoints
{job="wallet-api"} | json 
| operation="API_REQUEST_COMPLETED" 
| line_format "{{.path}} {{.duration}}" 
| pattern "<path> <duration>" 
| duration > 1000

# Step 2: Analyze specific slow endpoint
{job="wallet-api"} | json 
| path="/api/v1/wallets/transfer" 
| operation="API_REQUEST_COMPLETED" 
| duration > 2000
| line_format "{{.timestamp}} {{.traceId}} took {{.duration}}ms"

# Step 3: Deep dive into slow traces
{job="wallet-api"} | json 
| traceId="<slow_trace_id>" 
| logger=~".*Service.*|.*Repository.*"
| line_format "{{.timestamp}} {{.logger}} : {{.message}}"
```

### 3. Security Monitoring

**Goal**: Monitor for suspicious activity

**Security queries**:
```logql
# Multiple failed attempts from same user
{job="wallet-api"} | json 
| level="ERROR" 
| userId!="" 
| line_format "{{.timestamp}} {{.userId}} {{.message}}"

# Large transactions
{job="wallet-api"} | json 
| operation=~"DEPOSIT|WITHDRAW|TRANSFER" 
| amount > 10000
| line_format "{{.timestamp}} Large {{.operation}}: {{.amount}} trace={{.traceId}}"

# Unusual access patterns
{job="wallet-api"} | json 
| operation="API_REQUEST_RECEIVED" 
| client_ip!="" 
| line_format "{{.timestamp}} {{.client_ip}} {{.path}} {{.traceId}}"
```

## 📋 Best Practices

### 1. Query Optimization

- **Use specific time ranges**: Always limit queries to relevant time periods
- **Filter early**: Apply filters as early as possible in the query
- **Use structured parsing**: Leverage JSON parsing for better performance
- **Limit result sets**: Use `| limit 100` for large result sets

### 2. Dashboard Design

- **Group related metrics**: Organize panels by business function
- **Use appropriate visualizations**: Tables for details, graphs for trends
- **Set meaningful time ranges**: Default to relevant business hours
- **Add drill-down capabilities**: Link panels to detailed views

### 3. Alerting Strategy

- **Set appropriate thresholds**: Based on historical data and business requirements
- **Use multiple severity levels**: Critical, warning, info
- **Include context in alerts**: Provide relevant trace IDs and timestamps
- **Test alert conditions**: Regularly verify alerts trigger correctly

---

## 🌍 Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [Consultas e Monitoramento em Português](../pt/consultas-monitoramento.md)

---

*For more information, see the [main project documentation](../../../README.md).*
