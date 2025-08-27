# 💾 Redis Cache & Performance

This section covers the distributed Redis cache implementation in the Digital Wallet API, including setup, configuration, and performance optimization strategies.

## 📋 Quick Navigation

| 📄 Document | 📝 Description | 🎯 Audience |
|-------------|----------------|-------------|
| [Redis Cache Setup](redis-cache-setup.md) | Complete Redis cache implementation guide | Developers, DevOps |

## 🚀 Cache Overview

### Distributed Caching Strategy
- **Redis-based** distributed cache for scalability
- **Financial industry TTL standards** (30s-15min)
- **Hierarchical key naming** for organization
- **Automatic cache versioning** for deployments
- **JDK serialization** for reliability

### Cache Regions & TTLs

| Cache Region | TTL | Use Case | Performance Impact |
|--------------|-----|----------|-------------------|
| `wallet-list` | 3 minutes | Collection of wallets | High - Reduces DB load |
| `wallet-single` | 1 minute | Individual wallet data | Medium - Frequent updates |
| `wallet-balance` | 30 seconds | Critical financial data | Critical - Real-time accuracy |
| `wallet-transactions` | 10 minutes | Historical data | Low - Rarely changes |
| `user-profile` | 15 minutes | User information | Low - Static data |

## 🎯 Getting Started

### For Developers
1. **Setup Redis**: [Complete Setup Guide](redis-cache-setup.md)
2. **Configure environment**: [Environment Variables](redis-cache-setup.md#environment-variables)
3. **Test cache**: [Validation Methodology](redis-cache-setup.md)

### For DevOps
1. **Production setup**: [Redis Configuration](redis-cache-setup.md#production-considerations)
2. **Monitoring**: [Cache Monitoring](redis-cache-setup.md#monitoring-and-troubleshooting)
3. **Troubleshooting**: [Common Issues](redis-cache-setup.md)

## 🏗️ Architecture Integration

### Hexagonal Architecture
- **Domain Layer**: Cache-agnostic business logic
- **Application Layer**: Cache annotations (`@Cacheable`, `@CacheEvict`)
- **Infrastructure Layer**: Redis configuration and connection management

### Service Integration
```java
@Cacheable(value = "wallet-single", key = "#walletId")
public Wallet findById(UUID walletId) {
    // Business logic - cached automatically
}

@CacheEvict(value = {"wallet-list", "wallet-single"}, key = "#walletId")
public void updateWallet(UUID walletId) {
    // Cache invalidation on updates
}
```

## 🔧 Configuration Highlights

### Environment-Based Configuration
```yaml
app:
  cache:
    version: ${APP_CACHE_VERSION}
    ttl:
      default: ${CACHE_TTL_DEFAULT_MINUTES}
      wallet-balance: ${CACHE_TTL_WALLET_BALANCE_SECONDS}
      # ... other configurable TTLs
```

### Production-Ready Features
- **Connection pooling** for high performance
- **Automatic failover** and retry mechanisms
- **Cache versioning** for zero-downtime deployments
- **Comprehensive monitoring** and health checks

## 📊 Performance Benefits

### Database Load Reduction
- **Up to 80% reduction** in database queries for cached data
- **Faster response times** for frequently accessed data
- **Improved scalability** for high-traffic scenarios

### Financial Data Compliance
- **Conservative TTLs** ensure data freshness
- **Immediate invalidation** on financial operations
- **Audit trail** through structured logging

## 🔗 Related Documentation

- **🏠 Main Documentation**: [Project README](../../../README.md)
- **⚙️ Configuration**: [Environment Setup](../../configuration/en/environment-setup.md)
- **🔒 Security**: [Security Configuration](../../security/en/security-config.md)
- **📊 Monitoring**: [Observability Setup](../../monitoring/en/)

## 🛡️ Best Practices

### Development
- Test cache behavior in local environment
- Monitor cache hit/miss ratios
- Validate TTL configurations
- Use provided validation scripts

### Production
- Monitor Redis memory usage
- Set up alerts for cache failures
- Regular backup and recovery testing
- Performance tuning based on metrics

---

## 🌍 Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [README em Português](../pt/README.md)

---

*For more information, see the [main project documentation](../../../README.md).*
