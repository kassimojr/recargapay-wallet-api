# Tracing Troubleshooting Guide

## 🔍 Common Issues and Solutions

### 1. TraceId/SpanId Showing as Zeros

**Symptoms:**
```
DEBUG [wallet-api] [00000000000000000000000000000000,0000000000000000] [http-nio-8080-exec-1] c.r.w.i.logging.TraceContextFilter
```

**Possible Causes:**
- OpenTelemetry SDK not properly initialized
- TraceContextFilter not creating spans correctly
- MDC not being populated

**Solutions:**
1. **Verify OpenTelemetry Bean Creation:**
   ```bash
   # Check application startup logs for OpenTelemetry initialization
   grep -i "opentelemetry" logs/wallet-api*.log
   ```

2. **Check TraceContextFilter Registration:**
   ```bash
   # Verify filter is being called
   grep "TraceContextFilter" logs/wallet-api*.log
   ```

3. **Validate Span Creation:**
   ```java
   // Add debug logging in TraceContextFilter
   log.debug("Span context valid: {}", serverSpan.getSpanContext().isValid());
   log.debug("Generated traceId: {}, spanId: {}", traceId, spanId);
   ```

### 2. Missing TraceId/SpanId in Logs

**Symptoms:**
```
DEBUG [wallet-api] [,] [http-nio-8080-exec-1] c.r.w.core.services.DepositService
```

**Possible Causes:**
- MDC not properly set in TraceContextFilter
- Thread context not propagated
- Logback configuration missing MDC fields

**Solutions:**
1. **Verify MDC Population:**
   ```java
   // Add debug logging in TraceContextFilter
   log.debug("MDC traceId: {}, spanId: {}", MDC.get("traceId"), MDC.get("spanId"));
   ```

2. **Check Logback Configuration:**
   ```xml
   <!-- Ensure pattern includes MDC fields -->
   <pattern>[%X{traceId:-},%X{spanId:-}]</pattern>
   ```

3. **Verify Thread Context:**
   ```java
   // In service methods, check if MDC is available
   String traceId = MDC.get("traceId");
   if (traceId == null) {
       log.warn("TraceId not found in MDC");
   }
   ```

### 3. Application Startup Failures

**Symptoms:**
```
Failed to instantiate [io.opentelemetry.api.OpenTelemetry]: Factory method 'openTelemetry' threw exception
```

**Possible Causes:**
- Missing OpenTelemetry dependencies
- Conflicting OpenTelemetry versions
- Invalid configuration

**Solutions:**
1. **Verify Dependencies:**
   ```bash
   # Check if all required dependencies are present
   ./mvnw dependency:tree | grep opentelemetry
   ```

2. **Check Version Compatibility:**
   ```xml
   <!-- Ensure consistent OpenTelemetry version -->
   <opentelemetry.version>1.32.0</opentelemetry.version>
   ```

3. **Validate Configuration:**
   ```java
   // Simplify OpenTelemetryConfig if needed
   @Bean
   public OpenTelemetry openTelemetry() {
       return OpenTelemetrySdk.builder()
           .setTracerProvider(SdkTracerProvider.builder().build())
           .build();
   }
   ```

### 4. Performance Issues

**Symptoms:**
- Increased response times
- High CPU usage
- Memory leaks

**Possible Causes:**
- Too many spans being created
- Spans not being properly closed
- Memory not being released

**Solutions:**
1. **Monitor Span Creation:**
   ```java
   // Add metrics to track span creation
   private final Counter spanCreationCounter = Counter.builder("spans.created").register(meterRegistry);
   ```

2. **Verify Span Cleanup:**
   ```java
   // Ensure spans are always closed in finally blocks
   try (Scope scope = span.makeCurrent()) {
       // ... operation
   } finally {
       span.end(); // Always close spans
   }
   ```

3. **Check Memory Usage:**
   ```bash
   # Monitor memory usage
   jstat -gc <pid> 5s
   ```

### 5. OTLP Exporter Errors

**Symptoms:**
```
ERROR [wallet-api] [,] [OkHttp http://localhost:4317/...] i.o.e.internal.http.HttpExporter : Failed to export spans
```

**Possible Causes:**
- OpenTelemetry Collector not running
- Incorrect OTLP endpoint configuration
- Network connectivity issues

**Solutions:**
1. **Disable OTLP Exporter (Current Implementation):**
   ```java
   // Remove OTLP exporter from configuration
   SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
       .setResource(resource)
       // No span processors = local-only
       .build();
   ```

2. **Start OpenTelemetry Collector (If Needed):**
   ```bash
   # Using Docker
   docker run -p 4317:4317 -p 4318:4318 otel/opentelemetry-collector-contrib:latest
   ```

3. **Configure Alternative Endpoint:**
   ```yaml
   otel:
     exporter:
       otlp:
         endpoint: http://your-collector:4317
   ```

## 🔧 Debugging Tools

### 1. Log Analysis Commands

```bash
# Find all logs for a specific trace
grep "traceId_here" logs/wallet-api*.json

# Count unique trace IDs in logs
cat logs/wallet-api.json | jq -r '.traceId' | sort | uniq | wc -l

# Find traces with errors
cat logs/wallet-api.json | jq 'select(.level == "ERROR")' | jq -r '.traceId'

# Analyze trace distribution
cat logs/wallet-api.json | jq -r '.traceId' | sort | uniq -c | sort -nr
```

### 2. Application Health Checks

```bash
# Check if tracing filter is active
curl -v http://localhost:8080/actuator/health

# Verify OpenTelemetry metrics
curl http://localhost:8080/actuator/prometheus | grep otel

# Test trace generation
curl -X POST http://localhost:8080/api/v1/wallets/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"walletId": "12345678-1234-1234-1234-123456789012", "amount": 100.00}'
```

### 3. JVM Monitoring

```bash
# Monitor JVM threads
jstack <pid> | grep -A 5 -B 5 "TraceContext"

# Check memory usage
jmap -histo <pid> | grep -i span

# Monitor GC activity
jstat -gc <pid> 1s 10
```

## 📊 Monitoring and Alerting

### 1. Key Metrics to Monitor

```yaml
# Prometheus metrics to track
metrics:
  - name: trace_generation_rate
    description: Rate of trace ID generation
    query: rate(traces_created_total[5m])
  
  - name: span_creation_rate
    description: Rate of span creation
    query: rate(spans_created_total[5m])
  
  - name: trace_context_errors
    description: Errors in trace context propagation
    query: rate(trace_context_errors_total[5m])
```

### 2. Alerting Rules

```yaml
# Alert if trace generation stops
- alert: TracingDown
  expr: rate(traces_created_total[5m]) == 0
  for: 2m
  labels:
    severity: critical
  annotations:
    summary: "Distributed tracing is not generating traces"

# Alert if too many context errors
- alert: TraceContextErrors
  expr: rate(trace_context_errors_total[5m]) > 0.1
  for: 1m
  labels:
    severity: warning
  annotations:
    summary: "High rate of trace context errors"
```

### 3. Dashboard Queries

```promql
# Trace generation rate
rate(traces_created_total[5m])

# Average request duration by endpoint
histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))

# Error rate by trace
rate(http_requests_total{status=~"5.."}[5m]) / rate(http_requests_total[5m])
```

## 🚨 Emergency Procedures

### 1. Disable Tracing Quickly

If tracing is causing issues, disable it quickly:

```java
// Option 1: Disable filter registration
@ConditionalOnProperty(name = "tracing.enabled", havingValue = "true", matchIfMissing = false)
@Component
public class TraceContextFilter extends OncePerRequestFilter {
    // ... filter implementation
}
```

```yaml
# Option 2: Disable via configuration
tracing:
  enabled: false
```

### 2. Rollback to Previous Version

```bash
# Quick rollback using Git
git revert <commit-hash-of-tracing-changes>
git push origin main

# Redeploy application
./mvnw clean package
java -jar target/digital-wallet-api-*.jar
```

### 3. Temporary Workarounds

```java
// Temporary: Use static trace ID if generation fails
private String generateTraceId() {
    try {
        UUID uuid = UUID.randomUUID();
        return String.format("%016x%016x", uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    } catch (Exception e) {
        log.warn("Failed to generate trace ID, using fallback", e);
        return "fallback-trace-" + System.currentTimeMillis();
    }
}
```

## 📋 Health Check Checklist

### Daily Checks
- [ ] Verify trace IDs are being generated (not zeros)
- [ ] Check error logs for tracing-related issues
- [ ] Monitor application performance metrics
- [ ] Validate log correlation is working

### Weekly Checks
- [ ] Review trace generation patterns
- [ ] Analyze performance impact
- [ ] Check for memory leaks
- [ ] Update monitoring dashboards

### Monthly Checks
- [ ] Review and update documentation
- [ ] Analyze trace data for insights
- [ ] Plan optimizations if needed
- [ ] Update alerting rules

## 📞 Escalation Procedures

### Level 1: Application Issues
1. Check application logs
2. Verify configuration
3. Restart application if needed
4. Monitor for resolution

### Level 2: Performance Issues
1. Analyze performance metrics
2. Check resource utilization
3. Consider temporary tracing disable
4. Plan optimization

### Level 3: System-wide Issues
1. Escalate to infrastructure team
2. Consider rollback procedures
3. Implement emergency fixes
4. Post-incident review

---

## 🌍 Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [Solução de Problemas em Português](../pt/solucao-problemas.md)

---

*For more information, see the [main project documentation](../../../README.md).*
