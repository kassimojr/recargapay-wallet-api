# Improved Business Operation Logging Features

This document describes the enhanced logging system implemented for the RecargaPay Wallet API, providing better visibility and querying capabilities in Grafana Loki.

## 🎯 Overview

The logging system has been significantly improved to make business operation fields (`operation`, `status`, `walletId`) directly visible in the Grafana Loki interface, eliminating the need for complex JSON extraction queries.

## ✨ Key Improvements

### **Direct Field Visibility**
Business operation fields are now available as **first-class fields** in Grafana:

| Field | Description | Values | Visibility |
|-------|-------------|--------|------------|
| `operation` | Business operation type | `DEPOSIT`, `WITHDRAW`, `TRANSFER`, `API_REQUEST_COMPLETED` | ✅ Direct Label |
| `status` | Operation status | `START`, `SUCCESS`, `ERROR` | ✅ Direct Label |
| `walletId` | Wallet identifier | UUID format | 🔍 JSON Extractable |
| `level` | Log level | `INFO`, `DEBUG`, `ERROR` | ✅ Direct Label |
| `logger` | Source class | Class name | ✅ Direct Label |
| `traceId` | Distributed trace ID | Hex string | 🔍 JSON Extractable |
| `spanId` | Span identifier | Hex string | 🔍 JSON Extractable |

### **Query Simplification**

#### **Before (Complex)**
```logql
# Required JSON extraction for all business operations
{job="wallet-api"} | json | operation=~"DEPOSIT.*"
{job="wallet-api"} | json | operation="DEPOSIT" | status="SUCCESS"
```

#### **After (Direct)**
```logql
# Direct label filtering - faster and simpler
{job="wallet-api", operation="DEPOSIT"}
{job="wallet-api", operation="DEPOSIT", status="SUCCESS"}
```

## 🔧 Technical Implementation

### **1. LoggingUtils Enhancement**
The `LoggingUtils.log()` method now adds key fields to the MDC (Mapped Diagnostic Context):

```java
// Fields added to MDC for direct visibility
MDC.put("operation", operation);
MDC.put("status", String.valueOf(data.get("status")));
MDC.put("walletId", String.valueOf(data.get("walletId")));

// Automatic cleanup to prevent context leakage
MDC.remove("operation");
MDC.remove("status");
MDC.remove("walletId");
```

### **2. Promtail Configuration**
Enhanced pipeline to extract fields from both main JSON and nested JSON (fallback):

```yaml
pipeline_stages:
  # Extract from main JSON structure (MDC fields)
  - json:
      expressions:
        operation: operation
        status: status
        walletId: walletId
        
  # Fallback: Extract from nested JSON in message field
  - regex:
      expression: '"operation":"(?P<nested_operation>[^"]*)"'
      source: message
      
  # Use fallback if main field is empty
  - template:
      source: operation
      template: '{{ if .operation }}{{ .operation }}{{ else }}{{ .nested_operation }}{{ end }}'
```

### **3. Log Structure**
Logs now have a dual structure for maximum compatibility:

```json
{
  "timestamp": "2025-08-09T02:08:31.604784-03:00",
  "level": "INFO",
  "logger": "com.recargapay.wallet.core.services.DepositService",
  "operation": "DEPOSIT",           // ✅ Direct field
  "status": "SUCCESS",              // ✅ Direct field  
  "walletId": "aaaaaaaa-...",       // ✅ Direct field
  "traceId": "859ad6f5...",         // ✅ Direct field
  "spanId": "4ca24ed9...",          // ✅ Direct field
  "message": "{\"operation\":\"DEPOSIT\",\"status\":\"SUCCESS\",...}", // 📦 Nested JSON
  "application": "wallet-api",
  "environment": "development"
}
```

## 📊 Query Examples

### **Basic Operations**
```logql
# All deposits
{job="wallet-api", operation="DEPOSIT"}

# Successful withdrawals
{job="wallet-api", operation="WITHDRAW", status="SUCCESS"}

# Transfer errors
{job="wallet-api", operation="TRANSFER", status="ERROR"}

# All business operations
{job="wallet-api"} | operation != ""
```

### **Combined Filters**
```logql
# Error logs for specific operation
{job="wallet-api", level="ERROR", operation="DEPOSIT"}

# Operations in specific time range
{job="wallet-api", operation="DEPOSIT"} [5m]

# Multiple status filtering
{job="wallet-api", operation="DEPOSIT"} | status =~ "SUCCESS|ERROR"
```

### **Advanced Queries with JSON Extraction**
```logql
# Operations on specific wallet
{job="wallet-api", operation="DEPOSIT"} | json | walletId="aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"

# High-value transactions
{job="wallet-api", operation="DEPOSIT"} | json | amount > 1000

# Trace correlation
{job="wallet-api", operation="DEPOSIT"} | json | traceId="859ad6f5630636b1e9b62696309e5e7e"
```

## 🚀 Performance Benefits

### **Query Performance**
- **Label Filtering**: Direct label queries are ~10x faster than JSON extraction
- **Index Usage**: Loki can use indexes for direct label filtering
- **Reduced Processing**: No need to parse JSON for basic filters

### **Resource Usage**
- **Lower CPU**: Reduced JSON parsing overhead
- **Better Caching**: Label-based queries cache more effectively
- **Reduced Memory**: Less temporary object creation

## 📈 Monitoring and Alerting

### **Business Metrics**
```logql
# Error rate by operation
sum(rate({job="wallet-api", status="ERROR"}[5m])) by (operation)

# Operation volume
sum(rate({job="wallet-api", operation="DEPOSIT"}[5m]))

# Success rate
sum(rate({job="wallet-api", status="SUCCESS"}[5m])) / 
sum(rate({job="wallet-api"} | operation != "" [5m]))
```

### **Alert Examples**
```yaml
# High error rate alert
- alert: HighBusinessOperationErrorRate
  expr: |
    sum(rate({job="wallet-api", status="ERROR"}[5m])) by (operation) > 0.1
  for: 2m
  labels:
    severity: warning
  annotations:
    summary: "High error rate for {{ $labels.operation }}"
```

## 🎯 Dashboard Creation

### **Recommended Panels**

1. **Operation Volume**
   ```logql
   sum(rate({job="wallet-api"} | operation != "" [5m])) by (operation)
   ```

2. **Success Rate**
   ```logql
   sum(rate({job="wallet-api", status="SUCCESS"}[5m])) by (operation) /
   sum(rate({job="wallet-api"} | operation != "" [5m])) by (operation)
   ```

3. **Error Distribution**
   ```logql
   sum(rate({job="wallet-api", status="ERROR"}[5m])) by (operation)
   ```

4. **Recent Operations**
   ```logql
   {job="wallet-api"} | operation != "" | line_format "{{.operation}}/{{.status}} - {{.message}}"
   ```

## 💡 Best Practices

### **Query Optimization**
1. **Use Direct Labels First**: Always prefer `{job="wallet-api", operation="DEPOSIT"}` over JSON extraction
2. **Combine Labels**: Use multiple labels for precise filtering: `{operation="DEPOSIT", status="SUCCESS"}`
3. **Time Ranges**: Always specify time ranges: `[5m]`, `[1h]`, etc.
4. **Avoid High Cardinality**: Don't use `walletId` or `traceId` as labels in aggregations

### **Monitoring Strategy**
1. **Business Metrics**: Focus on operation success/failure rates
2. **Performance Metrics**: Monitor processing times via JSON extraction
3. **Error Analysis**: Use direct labels for quick error identification
4. **Trace Correlation**: Use JSON extraction for detailed debugging

## 🔧 Troubleshooting

### **Fields Not Appearing**
1. **Check Application**: Ensure application was restarted after LoggingUtils changes
2. **Verify Promtail**: Confirm Promtail container was restarted
3. **Wait for Ingestion**: Allow 2-3 minutes for new logs to be processed

### **Empty Query Results**
1. **Time Range**: Verify selected time range includes recent activity
2. **Operation Activity**: Confirm business operations are being executed
3. **Basic Test**: Start with `{job="wallet-api"}` to verify log ingestion

### **Performance Issues**
1. **Use Labels**: Prefer direct labels over JSON extraction for filtering
2. **Limit Time Range**: Use specific time ranges to reduce query scope
3. **Avoid Wildcards**: Use exact matches when possible

## 🔄 Backward Compatibility

The improvements maintain full backward compatibility:

- **Existing Queries**: All previous JSON extraction queries continue to work
- **Log Format**: Original JSON structure preserved in `message` field
- **Field Names**: No changes to existing field names or structures
- **Promtail Config**: Includes fallback mechanisms for older log formats

## 📋 Migration Guide

### **For Existing Dashboards**
1. **Update Queries**: Replace JSON extraction with direct labels where possible
2. **Test Performance**: Verify improved query response times
3. **Maintain Fallbacks**: Keep JSON extraction for complex filters

### **For New Implementations**
1. **Start with Labels**: Use direct label filtering as primary approach
2. **JSON for Details**: Use JSON extraction only for specific value filtering
3. **Follow Examples**: Use provided query patterns as templates

## 🎉 Summary

The improved logging system provides:
- ✅ **Direct field visibility** in Grafana interface
- ✅ **Simplified queries** with better performance
- ✅ **Enhanced monitoring** capabilities
- ✅ **Backward compatibility** with existing systems
- ✅ **Better debugging** experience for developers

This enhancement significantly improves the observability and monitoring capabilities of the RecargaPay Wallet API while maintaining system stability and performance.
