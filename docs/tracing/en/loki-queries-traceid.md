# Loki Queries for TraceId and Business Operations

This document explains how to query logs by `traceId`, `spanId`, and business operations (`operation`) in Grafana Loki for the RecargaPay Wallet API.

## 🎯 Available Fields for Queries

### **Direct Fields (Visible in Interface)**
With the implemented improvements, the following fields are available **directly** in the Grafana interface:

- **`operation`**: Operation type (`DEPOSIT`, `WITHDRAW`, `TRANSFER`, `API_REQUEST_COMPLETED`)
- **`status`**: Operation status (`START`, `SUCCESS`, `ERROR`)
- **`walletId`**: Wallet ID (available for business operations)
- **`level`**: Log level (`INFO`, `DEBUG`, `ERROR`)
- **`logger`**: Class that generated the log
- **`traceId`**: Distributed tracing ID
- **`spanId`**: Current span ID

### **Nested JSON Fields**
Additional fields available via JSON extraction:
- **`amount`**: Transaction amount
- **`currency`**: Currency (BRL)
- **`transactionId`**: Generated transaction ID
- **`fromWalletId`** / **`toWalletId`**: For transfers
- **`errorType`**: Error type (when applicable)

## 📊 Direct Queries (Recommended)

### **By Operation**
```logql
# All deposit operations
{job="wallet-api", operation="DEPOSIT"}

# Successful deposit operations
{job="wallet-api", operation="DEPOSIT", status="SUCCESS"}

# Withdraw operations with errors
{job="wallet-api", operation="WITHDRAW", status="ERROR"}

# All transfers
{job="wallet-api", operation="TRANSFER"}
```

### **By Log Level**
```logql
# Error logs with specific operation
{job="wallet-api", level="ERROR", operation="DEPOSIT"}

# Info logs for all operations
{job="wallet-api", level="INFO"} | operation != ""
```

## 🔍 Queries with JSON Extraction

### **By Specific Values**
```logql
# Operations on specific wallet
{job="wallet-api", operation="DEPOSIT"} | json | walletId="aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"

# Transactions above certain amount
{job="wallet-api", operation="DEPOSIT"} | json | amount > 100

# Operations with specific currency
{job="wallet-api"} | json | currency="BRL"
```

### **By TraceId (Distributed Tracing)**
```logql
# All logs from a specific journey
{job="wallet-api"} | json | traceId="859ad6f5630636b1e9b62696309e5e7e"

# Logs from specific operation by traceId
{job="wallet-api", operation="DEPOSIT"} | traceId="859ad6f5630636b1e9b62696309e5e7e"
```

## 📈 Advanced Queries

### **Performance Analysis**
```logql
# Operations that took more than 1 second
{job="wallet-api", operation="DEPOSIT"} | json | processingTimeMs > 1000

# Average processing time per operation
rate({job="wallet-api", operation="DEPOSIT"}[5m]) | json | avg(processingTimeMs)
```

### **Error Monitoring**
```logql
# All business operation errors
{job="wallet-api", status="ERROR"}

# Error rate by operation
sum(rate({job="wallet-api", status="ERROR"}[5m])) by (operation)
```

## 🔧 Benefits of Improvements

### **Before (Complex Query)**
```logql
# JSON extraction was required for everything
{job="wallet-api"} | json | operation=~"DEPOSIT.*"
```

### **Now (Direct Query)**
```logql
# Direct and more efficient query
{job="wallet-api", operation="DEPOSIT"}
```

### **Advantages**
1. **🚀 Performance**: Direct queries are faster
2. **👁️ Visibility**: Fields appear as columns in the interface
3. **🎯 Filters**: Direct filters in Grafana panel
4. **📊 Dashboards**: More efficient dashboard creation
5. **🔍 Debugging**: Quick problem identification

## 💡 Usage Tips

### **Combined Filters**
```logql
# Deposit operations on specific wallet in last 5 minutes
{job="wallet-api", operation="DEPOSIT"} | json | walletId="aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa" [5m]
```

### **Aggregations**
```logql
# Count operations by status
sum by (status) (count_over_time({job="wallet-api", operation="DEPOSIT"}[1h]))

# Total transaction volume
sum(rate({job="wallet-api", operation="DEPOSIT"}[5m])) | json | sum(amount)
```

## ⚠️ Important Notes

1. **Labels vs JSON**: Use labels (`operation`, `status`) for main filters and JSON for specific values
2. **Performance**: Avoid using `walletId` as label (high cardinality)
3. **Time Intervals**: Use time intervals to limit query scope
4. **Case Sensitive**: Field names are case sensitive

## 🛠️ Troubleshooting

### **If fields don't appear**
1. Check if application was restarted after improvements
2. Confirm Promtail was restarted
3. Wait a few minutes for new log ingestion

### **If queries return empty**
1. Check selected time interval
2. Confirm operations are being executed
3. Use `{job="wallet-api"}` first to verify logs are arriving

### **For debugging**
```logql
# Check if logs are being ingested
{job="wallet-api"} | limit 10

# Check log structure
{job="wallet-api", operation="DEPOSIT"} | limit 1
```

---

## 🌍 Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [Consultas Loki TraceID em Português](../pt/consultas-loki-traceid.md)

---

*For more information, see the [main project documentation](../../../README.md).*
