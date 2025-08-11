# Current Tracing Implementation

## 📋 Overview

The Recargapay Wallet API currently implements **basic distributed tracing** with the following characteristics:

- **Single span per HTTP request**: One server span created for each incoming request
- **Unique trace and span IDs**: Generated using UUID-based algorithm
- **MDC integration**: TraceId and spanId available in all structured logs
- **Manual OpenTelemetry setup**: No auto-configuration dependencies
- **Local-only operation**: No external trace collectors required

## 🏗️ Architecture

### Components

1. **TraceContextFilter** - Main tracing filter
2. **OpenTelemetryConfig** - Manual SDK configuration
3. **Logback integration** - Structured JSON logging with trace context

### Request Flow

```
HTTP Request → TraceContextFilter → Application Logic → HTTP Response
     ↓              ↓                      ↓               ↓
  Generate      Set MDC           Use traceId/spanId    Clean MDC
 traceId/spanId  Context          in all logs          Context
```

## 🔧 Implementation Details

### 1. TraceContextFilter.java

**Location**: `src/main/java/com/recargapay/wallet/infra/logging/TraceContextFilter.java`

**Key Features**:
- Creates one OpenTelemetry server span per HTTP request
- Generates unique traceId (32 hex chars) and spanId (16 hex chars) using UUID
- Populates SLF4J MDC with trace context
- Handles span lifecycle (start, current, end)
- Records HTTP attributes (method, URL, status code)

**Code Flow**:
```java
// 1. Extract context from headers (for distributed tracing)
Context parentContext = openTelemetry.getPropagators()
    .getTextMapPropagator()
    .extract(Context.current(), request, httpServletRequestGetter);

// 2. Create server span
Span serverSpan = tracer.spanBuilder(request.getMethod() + " " + request.getRequestURI())
    .setParent(parentContext)
    .setSpanKind(SpanKind.SERVER)
    .startSpan();

// 3. Set span as current and populate MDC
try (Scope scope = serverSpan.makeCurrent()) {
    MDC.put("traceId", traceId);
    MDC.put("spanId", spanId);
    
    // Continue request processing
    filterChain.doFilter(request, response);
}
```

### 2. OpenTelemetryConfig.java

**Location**: `src/main/java/com/recargapay/wallet/config/OpenTelemetryConfig.java`

**Key Features**:
- Manual OpenTelemetry SDK initialization
- No external span export (local-only)
- W3C trace context propagation support
- Service resource identification

**Configuration**:
```java
@Bean
public OpenTelemetry openTelemetry() {
    Resource resource = Resource.getDefault()
        .merge(Resource.create(Attributes.of(
            ResourceAttributes.SERVICE_NAME, "recargapay-wallet-api",
            ResourceAttributes.SERVICE_VERSION, "1.0.0"
        )));

    SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
        .setResource(resource)
        .build(); // No span processors = local-only

    return OpenTelemetrySdk.builder()
        .setTracerProvider(tracerProvider)
        .setPropagators(contextPropagators)
        .build();
}
```

### 3. Logback Integration

**Location**: `src/main/resources/logback-spring.xml`

**Key Features**:
- Structured JSON logging with trace context
- Console and file appenders
- MDC fields inclusion (traceId, spanId)

**Log Pattern**:
```xml
<pattern>%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} %highlight(%-5level) [${springAppName:-wallet-api}] [%X{traceId:-},%X{spanId:-}] [%thread] %logger{36} : %msg%n</pattern>
```

## 📊 Current Behavior

### Log Output Example

```
2025-07-22T03:27:20.123-03:00 DEBUG [wallet-api] [6ed5c3ad90a37e0437be0bf3f15cb9d6,6425741b3b8a7581] [http-nio-8080-exec-1] c.r.w.i.logging.TraceContextFilter : Request received: POST /api/v1/wallets/deposit, traceId=6ed5c3ad90a37e0437be0bf3f15cb9d6, spanId=6425741b3b8a7581

2025-07-22T03:27:20.125-03:00 INFO  [wallet-api] [6ed5c3ad90a37e0437be0bf3f15cb9d6,6425741b3b8a7581] [http-nio-8080-exec-1] c.r.w.i.l.ApiLoggingInterceptor : {"traceId":"6ed5c3ad90a37e0437be0bf3f15cb9d6","spanId":"6425741b3b8a7581","operation":"API_REQUEST_RECEIVED","method":"POST","path":"/api/v1/wallets/deposit"}

2025-07-22T03:27:20.130-03:00 DEBUG [wallet-api] [6ed5c3ad90a37e0437be0bf3f15cb9d6,6425741b3b8a7581] [http-nio-8080-exec-1] c.r.w.core.services.DepositService : Starting deposit operation for wallet: 12345

2025-07-22T03:27:20.145-03:00 DEBUG [wallet-api] [6ed5c3ad90a37e0437be0bf3f15cb9d6,6425741b3b8a7581] [http-nio-8080-exec-1] c.r.w.adapter.repositories.impl.WalletRepositoryImpl : Executing database query for wallet: 12345
```

### Key Observations

- **Same traceId**: `6ed5c3ad90a37e0437be0bf3f15cb9d6` across all logs in the request
- **Same spanId**: `6425741b3b8a7581` across all logs in the request
- **Perfect correlation**: All logs from the same HTTP request can be grouped by traceId
- **Thread consistency**: Same thread ID throughout the request processing

## ✅ Advantages

1. **Simple and reliable**: Minimal complexity, easy to understand
2. **Perfect correlation**: All logs from one request share the same traceId
3. **No external dependencies**: Works without OpenTelemetry collectors
4. **Low performance impact**: Single span creation per request
5. **Debugging friendly**: Easy to trace request flow through logs

## ⚠️ Limitations

1. **No granular timing**: Cannot measure time spent in each layer
2. **No operation isolation**: Cannot distinguish Controller vs Service vs Repository operations
3. **Limited observability**: Missing detailed performance insights
4. **Single span context**: All operations share the same spanId

## 🔍 Use Cases

### Perfect for:
- **Log correlation**: Finding all logs related to a specific request
- **Error debugging**: Tracing the complete flow of a failed request
- **Basic monitoring**: Understanding request patterns and volumes
- **Compliance**: Meeting basic distributed tracing requirements

### Not ideal for:
- **Performance optimization**: Identifying bottlenecks in specific layers
- **Advanced APM**: Detailed application performance monitoring
- **Service mesh integration**: Complex microservices tracing
- **Real-time monitoring**: Live performance dashboards

## 📈 Performance Metrics

- **Overhead per request**: < 1ms
- **Memory impact**: Minimal (single span object)
- **CPU impact**: Negligible UUID generation
- **Storage impact**: ~100 bytes per request in logs

## 🔧 Configuration Files

### Dependencies (pom.xml)
```xml
<!-- Core OpenTelemetry dependencies -->
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-api</artifactId>
    <version>${opentelemetry.version}</version>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-sdk</artifactId>
    <version>${opentelemetry.version}</version>
</dependency>
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-logback-mdc-1.0</artifactId>
    <version>${opentelemetry.version}-alpha</version>
</dependency>
```

### Application Configuration (application.yml)
```yaml
# OpenTelemetry Configuration
otel:
  sdk:
    disabled: true  # Disable auto-configuration
  spring:
    enabled: false  # Disable Spring integration
    webmvc:
      enabled: false # Use our custom filter instead
  propagators: tracecontext,baggage
  metrics:
    exporter: none
  logs:
    exporter: none
```

## 🚀 Getting Started

### 1. Verify Implementation
Check if tracing is working by making a request and looking for traceId/spanId in logs:

```bash
curl -X POST http://localhost:8080/api/v1/wallets/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"walletId": "12345678-1234-1234-1234-123456789012", "amount": 100.00}'
```

### 2. Search Logs by TraceId
```bash
# Find all logs for a specific trace
grep "6ed5c3ad90a37e0437be0bf3f15cb9d6" logs/wallet-api*.json

# Or using jq for JSON logs
cat logs/wallet-api.json | jq 'select(.traceId == "6ed5c3ad90a37e0437be0bf3f15cb9d6")'
```

### 3. Monitor Trace Generation
Watch logs in real-time to see trace IDs being generated:
```bash
tail -f logs/wallet-api.json | jq '{timestamp, traceId, spanId, message}'
```

---

## 🌍 Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [Implementação Atual em Português](../pt/implementacao-atual.md)

---

*For more information, see the [main project documentation](../../../README.md).*
