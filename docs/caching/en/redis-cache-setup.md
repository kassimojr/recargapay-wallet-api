# Redis Distributed Cache Configuration Guide

## Overview

This guide explains how to set up and use Redis distributed caching in the Digital Wallet API following **industry best practices**. Redis caching improves performance by storing frequently accessed data in memory, reducing database queries and improving response times.

The implementation follows **financial industry standards** with:
- **Hierarchical key naming** (`namespace:entity:operation:version`)
- **Conservative TTLs** for financial data consistency
- **JDK serialization** for reliable object serialization/deserialization
- **Distributed caching** across multiple application instances
- **Automatic cache versioning** for deployment invalidation

## Configuration

### Environment Variables

Add the following Redis configuration to your `.env` file:

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=digital_wallet
DB_USERNAME=your_db_username
DB_PASSWORD=your_secure_db_password

# JWT Configuration
JWT_SECRET=your_super_secure_jwt_secret_key_here_minimum_256_bits

# Redis Configuration for Distributed Caching
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redis_secure_password_here

# Cache Configuration
APP_CACHE_VERSION=v1

# Cache TTL Configuration (Financial Industry Standards)
CACHE_TTL_DEFAULT_MINUTES=2
CACHE_TTL_WALLET_LIST_MINUTES=3
CACHE_TTL_WALLET_SINGLE_MINUTES=1
CACHE_TTL_WALLET_BALANCE_SECONDS=30
CACHE_TTL_WALLET_TRANSACTIONS_MINUTES=10
CACHE_TTL_USER_PROFILE_MINUTES=15

# Admin User Configuration
ADMIN_USERNAME=your_admin_username
ADMIN_PASSWORD=your_secure_admin_password

# Application Configuration
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev

# Logging Configuration
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_APP=DEBUG
```

### Application Configuration

The cache configuration is now **fully configurable** via environment variables in `application.yml`:

```yaml
# Application Configuration
app:
  # User configuration
  user:
    username: ${ADMIN_USERNAME}
    password: ${ADMIN_PASSWORD}
  
  # Cache configuration
  cache:
    version: ${APP_CACHE_VERSION}
    ttl:
      # TTL values in minutes/seconds following financial industry standards
      default: ${CACHE_TTL_DEFAULT_MINUTES}
      wallet-list: ${CACHE_TTL_WALLET_LIST_MINUTES}
      wallet-single: ${CACHE_TTL_WALLET_SINGLE_MINUTES}
      wallet-balance: ${CACHE_TTL_WALLET_BALANCE_SECONDS}
      wallet-transactions: ${CACHE_TTL_WALLET_TRANSACTIONS_MINUTES}
      user-profile: ${CACHE_TTL_USER_PROFILE_MINUTES}
```

### Docker Setup

Redis is automatically configured in `docker-compose.yml`:

```yaml
redis:
  image: redis:7-alpine
  container_name: wallet-redis
  restart: unless-stopped
  ports:
    - "6379:6379"
  command: redis-server --appendonly yes
  volumes:
    - redis_data:/data
  healthcheck:
    test: ["CMD", "redis-cli", "ping"]
    interval: 10s
    timeout: 5s
    retries: 5
```

### Cache Architecture

### Cache Regions and TTLs (Financial Industry Standards)

The cache is organized into regions with **conservative TTLs** appropriate for financial data:

| **Cache Region** | **Purpose** | **TTL** | **Key Pattern** |
|------------------|-------------|---------|-----------------|
| `wallet-list` | Wallet collections | configurable | `wallet-api:wallet-list:v1:all` |
| `wallet-single` | Individual wallets | configurable | `wallet-api:wallet-single:v1:{walletId}` |
| `wallet-balance` | Balance data | configurable | `wallet-api:wallet-balance:v1:{walletId}` |
| `wallet-transactions` | Transaction history | configurable | `wallet-api:wallet-transactions:v1:{walletId}` |
| `user-profile` | User profiles | configurable | `wallet-api:user-profile:v1:{userId}` |

### Key Naming Convention

Following **industry best practices**, cache keys use hierarchical naming:

```
{application}:{cache-region}:{version}:{identifier}
```

**Examples:**
- `wallet-api:wallet-list:v1:all`
- `wallet-api:wallet-single:v1:123e4567-e89b-12d3-a456-426614174000`
- `wallet-api:wallet-balance:v1:123e4567-e89b-12d3-a456-426614174000`

### Cache Configuration Class

The `CacheConfig` class configures Redis cache manager with **industry standards** and **configurable TTLs**:

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${spring.application.name}")
    private String applicationName;
    
    @Value("${app.cache.version}")
    private String cacheVersion;
    
    // Cache TTL configuration from environment variables (in minutes/seconds)
    @Value("${CACHE_TTL_DEFAULT_MINUTES}")
    private int defaultTtlMinutes;
    
    @Value("${CACHE_TTL_WALLET_LIST_MINUTES}")
    private int walletListTtlMinutes;
    
    @Value("${CACHE_TTL_WALLET_SINGLE_MINUTES}")
    private int walletSingleTtlMinutes;
    
    @Value("${CACHE_TTL_WALLET_BALANCE_SECONDS}")
    private int walletBalanceSeconds;
    
    @Value("${CACHE_TTL_WALLET_TRANSACTIONS_MINUTES}")
    private int walletTransactionsTtlMinutes;
    
    @Value("${CACHE_TTL_USER_PROFILE_MINUTES}")
    private int userProfileTtlMinutes;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        JdkSerializationRedisSerializer jdkSerializer = new JdkSerializationRedisSerializer();
        
        // Default cache configuration with configurable TTL for financial data
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(defaultTtlMinutes))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jdkSerializer))
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> applicationName + ":" + cacheName + ":" + cacheVersion + ":");

        // Custom cache configurations following financial industry standards
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        cacheConfigurations.put("wallet-list", defaultConfig.entryTtl(Duration.ofMinutes(walletListTtlMinutes)));
        cacheConfigurations.put("wallet-single", defaultConfig.entryTtl(Duration.ofMinutes(walletSingleTtlMinutes)));
        cacheConfigurations.put("wallet-balance", defaultConfig.entryTtl(Duration.ofSeconds(walletBalanceSeconds)));
        cacheConfigurations.put("wallet-transactions", defaultConfig.entryTtl(Duration.ofMinutes(walletTransactionsTtlMinutes)));
        cacheConfigurations.put("user-profile", defaultConfig.entryTtl(Duration.ofMinutes(userProfileTtlMinutes)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}

## Service Implementation

### Cache Annotations

Services use Spring Cache annotations with **proper cache invalidation**:

#### Read Operations (Caching)

```java
// List all wallets
@Cacheable(value = "wallet-list", key = "'all'")
public List<Wallet> findAll() {
    return walletRepository.findAll();
}

// Find wallet by ID
@Cacheable(value = "wallet-single", key = "#walletId")
public Wallet findById(UUID walletId) {
    return walletRepository.findById(walletId)
        .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));
}
```

#### Write Operations (Cache Invalidation)

```java
// Create wallet - invalidates list and individual cache
@Caching(evict = {
    @CacheEvict(value = "wallet-list", key = "'all'"),
    @CacheEvict(value = "wallet-single", key = "#result.id", condition = "#result != null")
})
public Wallet create(Wallet wallet) {
    // Implementation
}

// Deposit - invalidates affected caches
@Caching(evict = {
    @CacheEvict(value = "wallet-list", key = "'all'"),
    @CacheEvict(value = "wallet-single", key = "#walletId")
})
public Transaction deposit(UUID walletId, BigDecimal amount) {
    // Implementation
}

// Transfer - invalidates both wallets
@Caching(evict = {
    @CacheEvict(value = "wallet-list", key = "'all'"),
    @CacheEvict(value = "wallet-single", key = "#fromWalletId"),
    @CacheEvict(value = "wallet-single", key = "#toWalletId")
})
public List<Transaction> transfer(UUID fromWalletId, UUID toWalletId, BigDecimal amount) {
    // Implementation
}

## Testing Cache Functionality

### Testing Cache Invalidation

1. **Create a wallet (invalidates cache):**
   ```bash
   curl -X POST -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{"userId":"123e4567-e89b-12d3-a456-426614174000","initialBalance":100.00}' \
        http://localhost:8080/api/wallets
   ```

2. **Verify cache was invalidated:**
   ```bash
   # This will be a cache miss due to invalidation
   curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/wallets
   ```

## ⚠️ **IMPORTANT: Correct Cache Validation Methodology**

### **🚨 Common Validation Problems**

Many developers face difficulties when validating Redis cache due to **timing and methodology issues**. This section explains how to validate correctly.

#### **❌ Incorrect Methodology (That DOESN'T Work):**

```bash
# PROBLEM: Manual testing with delays
1. Make request in Postman/Insomnia
2. [Time passes - switching applications]
3. [Time passes - opening terminal]
4. [Time passes - typing commands]
5. Check keys: KEYS wallet-api:*
6. Result: (empty array) ❌ - Key already expired!
```

**Why it fails:**
- **Short TTL**: 3 minutes for financial data
- **Human delay**: 2-5 minutes between request and verification
- **Wrong pattern**: `wallet-api:*` instead of `digital-wallet-api:*`

#### **✅ Correct Methodology (That Works):**

```bash
# SOLUTION: Automated testing without delays
# 1. Get token
TOKEN=$(curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' \
  -s | jq -r '.token')

# 2. Clear cache for clean test
docker exec wallet-redis redis-cli FLUSHALL

# 3. First request (cache miss) with timer
echo "=== FIRST REQUEST (CACHE MISS) ==="
start_time=$(date +%s%N)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/wallets -s > /dev/null
end_time=$(date +%s%N)
first_time=$(( (end_time - start_time) / 1000000 ))
echo "Time: ${first_time}ms"

# 4. Check key IMMEDIATELY (no delay)
echo "=== CHECKING CREATED KEY ==="
docker exec wallet-redis redis-cli KEYS "*"
docker exec wallet-redis redis-cli TTL "digital-wallet-api:wallet-list:v1:all"

# 5. Second request (cache hit) with timer
echo "=== SECOND REQUEST (CACHE HIT) ==="
start_time=$(date +%s%N)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/wallets -s > /dev/null
end_time=$(date +%s%N)
second_time=$(( (end_time - start_time) / 1000000 ))
echo "Time: ${second_time}ms"

# 6. Calculate improvement
improvement=$(( (first_time - second_time) * 100 / first_time ))
echo "=== RESULT ==="
echo "Cache Miss: ${first_time}ms"
echo "Cache Hit: ${second_time}ms"
echo "Improvement: ${improvement}%"
```

### **🔍 Correct Key Patterns**

#### **Keys Generated by Application:**

| **Operation** | **Generated Key** | **TTL** |
|---------------|-------------------|---------|
| Wallet list | `digital-wallet-api:wallet-list:v1:all` | configurable |
| Individual wallet | `digital-wallet-api:wallet-single:v1:{walletId}` | configurable |
| Wallet balance | `digital-wallet-api:wallet-balance:v1:{walletId}` | configurable |
| Transactions | `digital-wallet-api:wallet-transactions:v1:{walletId}` | configurable |
| User profile | `digital-wallet-api:user-profile:v1:{userId}` | configurable |

#### **Verification Commands:**

```bash
# ✅ CORRECT - Search all keys
docker exec wallet-redis redis-cli KEYS "*"

# ✅ CORRECT - Search application-specific keys
docker exec wallet-redis redis-cli KEYS "digital-wallet-api:*"

# ✅ CORRECT - Search specific region keys
docker exec wallet-redis redis-cli KEYS "digital-wallet-api:wallet-list:*"

# ❌ INCORRECT - Incomplete prefix
docker exec wallet-redis redis-cli KEYS "wallet-api:*"
```

### **⏰ Understanding TTLs and Timing**

#### **Configured TTLs (Financial Standard):**

```bash
# Check TTL of specific key
docker exec wallet-redis redis-cli TTL "digital-wallet-api:wallet-list:v1:all"

# Possible results:
# 180 = 3 minutes remaining
# 60 = 1 minute remaining
# -1 = Key without expiration (shouldn't happen)
# -2 = Key doesn't exist (expired or never created)
```

#### **Expiration Demonstration:**

```bash
# Real-time expiration test
echo "=== TTL DEMONSTRATION ==="

# Make request
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/wallets -s > /dev/null

# Check initial TTL
echo "Initial TTL:"
docker exec wallet-redis redis-cli TTL "digital-wallet-api:wallet-list:v1:all"

# Wait 1 minute
echo "Waiting 1 minute..."
sleep 60

# Check TTL after 1 minute
echo "TTL after 1 minute:"
docker exec wallet-redis redis-cli TTL "digital-wallet-api:wallet-list:v1:all"

# Wait 2 more minutes (total: 3 minutes)
echo "Waiting 2 more minutes..."
sleep 120

# Check if key expired
echo "Keys after 3 minutes (should be empty):"
docker exec wallet-redis redis-cli KEYS "digital-wallet-api:wallet-list:*"
```

### **🧪 Complete Test Script**

Create a `test-cache.sh` file for automated validation:

```bash
#!/bin/bash

echo "=== COMPLETE REDIS CACHE TEST ==="

# Configuration
API_URL="http://localhost:8080"
REDIS_CONTAINER="wallet-redis"

# Function to get token
get_token() {
    curl -X POST $API_URL/api/v1/auth/login \
        -H "Content-Type: application/json" \
        -d '{"username":"admin","password":"admin"}' \
        -s | jq -r '.token'
}

# Function to measure request time
measure_request() {
    local token=$1
    start_time=$(date +%s%N)
    curl -H "Authorization: Bearer $token" $API_URL/api/v1/wallets -s > /dev/null
    end_time=$(date +%s%N)
    echo $(( (end_time - start_time) / 1000000 ))
}

# 1. Get token
echo "1. Getting JWT token..."
TOKEN=$(get_token)
if [ "$TOKEN" = "null" ] || [ -z "$TOKEN" ]; then
    echo "❌ Error getting token. Check credentials."
    exit 1
fi
echo "✅ Token obtained successfully"

# 2. Clear cache
echo -e "\n2. Clearing cache for clean test..."
docker exec $REDIS_CONTAINER redis-cli FLUSHALL > /dev/null
echo "✅ Cache cleared"

# 3. First request (cache miss)
echo -e "\n3. First request (Cache Miss)..."
FIRST_TIME=$(measure_request $TOKEN)
echo "⏱️  Time: ${FIRST_TIME}ms"

# 4. Check created key
echo -e "\n4. Checking created key..."
KEYS=$(docker exec $REDIS_CONTAINER redis-cli KEYS "*")
if [ -z "$KEYS" ]; then
    echo "❌ No keys found! Cache may not be working."
    exit 1
fi
echo "✅ Key created: $KEYS"

# 5. Check TTL
TTL=$(docker exec $REDIS_CONTAINER redis-cli TTL "digital-wallet-api:wallet-list:v1:all")
echo "⏰ TTL: ${TTL} seconds"

# 6. Second request (cache hit)
echo -e "\n5. Second request (Cache Hit)..."
SECOND_TIME=$(measure_request $TOKEN)
echo "⚡ Time: ${SECOND_TIME}ms"

# 7. Calculate improvement
if [ $SECOND_TIME -lt $FIRST_TIME ]; then
    IMPROVEMENT=$(( (FIRST_TIME - SECOND_TIME) * 100 / FIRST_TIME ))
    echo -e "\n🎉 CACHE WORKING!"
    echo "📊 First request (Cache Miss): ${FIRST_TIME}ms"
    echo "📊 Second request (Cache Hit): ${SECOND_TIME}ms"
    echo "📈 Performance improvement: ${IMPROVEMENT}%"
else
    echo -e "\n⚠️  Similar times. Cache may be working but with small difference."
fi

# 8. Final information
echo -e "\n=== FINAL INFORMATION ==="
echo "🔑 Keys in Redis:"
docker exec $REDIS_CONTAINER redis-cli KEYS "*"
echo "📊 Redis statistics:"
docker exec $REDIS_CONTAINER redis-cli INFO keyspace
```

### **🔧 Diagnostic Commands**

#### **Connectivity Verification:**

```bash
# Test Redis connection
docker exec wallet-redis redis-cli ping
# Expected result: PONG

# Check application status
curl -s http://localhost:8080/actuator/health | jq .components.redis
# Expected result: {"status": "UP", "details": {"version": "7.4.5"}}
```

#### **Real-time Monitoring:**

```bash
# Monitor Redis commands in real-time
docker exec wallet-redis redis-cli MONITOR

# In another terminal, make requests and see commands being executed
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/wallets
```

#### **Performance Analysis:**

```bash
# Detailed Redis statistics
docker exec wallet-redis redis-cli INFO stats

# Memory information
docker exec wallet-redis redis-cli INFO memory

# Key information
docker exec wallet-redis redis-cli INFO keyspace
```

### **📋 Validation Checklist**

Use this checklist to ensure cache is working correctly:

- [ ] **Connectivity**: `docker exec wallet-redis redis-cli ping` returns `PONG`
- [ ] **Application**: `/actuator/health` shows Redis as `UP`
- [ ] **Authentication**: JWT token obtained successfully
- [ ] **Cache Miss**: First request creates key in Redis
- [ ] **Cache Hit**: Second request is faster
- [ ] **TTL**: Keys have appropriate TTL (configurable)
- [ ] **Naming**: Keys follow pattern `digital-wallet-api:*`
- [ ] **Expiration**: Keys expire after configured TTL
- [ ] **Invalidation**: Cache is cleared after write operations

### **🚨 Specific Troubleshooting**

#### **Problem: "Keys don't appear"**

```bash
# Diagnosis:
1. Check correct pattern: KEYS "*" (not "wallet-api:*")
2. Check timing: Verify immediately after request
3. Check TTL: docker exec wallet-redis redis-cli TTL "key"
4. Check authentication: Request should return HTTP 200
```

#### **Problem: "Cache doesn't improve performance"**

```bash
# Diagnosis:
1. Use automated timer (not manual)
2. Clear cache before test: FLUSHALL
3. Make multiple requests to see pattern
4. Check if data is being fetched from DB vs Redis
```

#### **Problem: "TTL too low"**

```bash
# Explanation:
- Configurable TTLs are used for financial data
- Industry standard: 30s-3min for critical data
- For testing: Use automated script, not manual verification
```

#### **Problem: Memory Issues**

```bash
# Explanation:
- Monitor cache size: `docker exec wallet-redis redis-cli INFO memory`
- Adjust TTLs to reduce memory usage
- Implement cache size limits if needed
```

#### **Problem: Serialization Errors**

```bash
# Explanation:
- Ensure entities implement `Serializable`
- Add `serialVersionUID` to entities
- Verify JDK serialization configuration
```

## Best Practices

### Financial Data Caching
1. **Conservative TTLs**: Use configurable TTLs for financial data
2. **Immediate Invalidation**: Always invalidate cache after balance changes
3. **Consistency Over Performance**: Prefer data accuracy over cache performance

### Key Management
1. **Hierarchical Naming**: Use `namespace:entity:operation:version` pattern
2. **Versioning**: Include version in keys for deployment invalidation
3. **Consistent Patterns**: Maintain consistent key naming across services

### Monitoring
1. **Cache Hit Rate**: Monitor and aim for >70% hit rate
2. **Memory Usage**: Set up alerts for Redis memory usage
3. **TTL Monitoring**: Verify keys are expiring as expected
4. **Error Tracking**: Monitor cache-related exceptions

### Security
1. **No Sensitive Data**: Never cache sensitive information like passwords
2. **Access Control**: Secure Redis instance in production
3. **Data Encryption**: Consider encryption for sensitive cached data

## Production Considerations

### Deployment
1. **Cache Warming**: Consider pre-loading critical data after deployment
2. **Gradual Rollout**: Test cache behavior in staging environment
3. **Monitoring**: Set up comprehensive cache monitoring

### Scaling
1. **Redis Cluster**: Consider Redis cluster for high availability
2. **Connection Pooling**: Tune connection pool settings for load
3. **Memory Management**: Plan Redis memory requirements

### Backup and Recovery
1. **AOF Persistence**: Enabled by default in our configuration
2. **Regular Backups**: Consider periodic Redis data backups
3. **Disaster Recovery**: Plan for Redis instance recovery

This implementation follows **industry best practices** used by companies like Netflix, Uber, and financial institutions, ensuring reliability, performance, and maintainability.

## Entity Serialization

Entities must implement `Serializable` for JDK serialization:

```java
@Entity
@Table(name = "wallets")
public class Wallet implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Entity fields and methods
}
```

## Usage Examples

### Testing Cache Functionality

1. **Start the application with Redis:**
   ```bash
   docker-compose up -d redis
   ./mvnw spring-boot:run
   ```

2. **Test cache hit/miss:**
   ```bash
   # First request (cache miss) - slower response
   curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/wallets
   
   # Second request (cache hit) - faster response
   curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/wallets
   ```

3. **Verify cache keys in Redis:**
   ```bash
   docker exec -it wallet-redis redis-cli
   > KEYS wallet-api:*
   > TTL wallet-api:wallet-list:v1:all
   ```

### Cache Invalidation Testing

1. **Create a wallet (invalidates cache):**
   ```bash
   curl -X POST -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{"userId":"123e4567-e89b-12d3-a456-426614174000","initialBalance":100.00}' \
        http://localhost:8080/api/wallets
   ```

2. **Verify cache was invalidated:**
   ```bash
   # This will be a cache miss due to invalidation
   curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/wallets
   ```

## Monitoring and Troubleshooting

### Redis Monitoring Commands

1. **Check Redis connection:**
   ```bash
   docker exec -it wallet-redis redis-cli ping
   ```

2. **Monitor cache operations:**
   ```bash
   docker exec -it wallet-redis redis-cli MONITOR
   ```

3. **View cache statistics:**
   ```bash
   docker exec -it wallet-redis redis-cli INFO stats
   ```

4. **List all cache keys:**
   ```bash
   docker exec -it wallet-redis redis-cli KEYS "wallet-api:*"
   ```

5. **Check specific key TTL:**
   ```bash
   docker exec -it wallet-redis redis-cli TTL "wallet-api:wallet-list:v1:all"
   ```

### Performance Metrics

Monitor these key metrics:

- **Cache Hit Rate**: Should be > 70% for optimal performance
- **Memory Usage**: Monitor Redis memory consumption
- **Key Expiration**: Verify TTLs are working correctly
- **Connection Pool**: Monitor active/idle connections

### Common Issues and Solutions

#### Issue: Cache Not Working
**Symptoms:** No performance improvement, always hitting database
**Solutions:**
1. Verify Redis is running: `docker ps | grep redis`
2. Check Redis connectivity: `docker exec -it wallet-redis redis-cli ping`
3. Verify `@EnableCaching` annotation is present
4. Check application logs for cache-related errors

#### Issue: Stale Data
**Symptoms:** Old data returned after updates
**Solutions:**
1. Verify `@CacheEvict` annotations on write operations
2. Check cache key patterns match between `@Cacheable` and `@CacheEvict`
3. Consider reducing TTL for frequently updated data

#### Issue: Memory Issues
**Symptoms:** Redis running out of memory
**Solutions:**
1. Monitor cache size: `docker exec -it wallet-redis redis-cli INFO memory`
2. Adjust TTLs to reduce memory usage
3. Implement cache size limits if needed

#### Issue: Serialization Errors
**Symptoms:** `ClassCastException` or serialization failures
**Solutions:**
1. Ensure entities implement `Serializable`
2. Add `serialVersionUID` to entities
3. Verify JDK serialization configuration

## Best Practices

### Financial Data Caching
1. **Conservative TTLs**: Use configurable TTLs for financial data
2. **Immediate Invalidation**: Always invalidate cache after balance changes
3. **Consistency Over Performance**: Prefer data accuracy over cache performance

### Key Management
1. **Hierarchical Naming**: Use `namespace:entity:operation:version` pattern
2. **Versioning**: Include version in keys for deployment invalidation
3. **Consistent Patterns**: Maintain consistent key naming across services

### Monitoring
1. **Cache Hit Rate**: Monitor and aim for >70% hit rate
2. **Memory Usage**: Set up alerts for Redis memory usage
3. **TTL Monitoring**: Verify keys are expiring as expected
4. **Error Tracking**: Monitor cache-related exceptions

### Security
1. **No Sensitive Data**: Never cache sensitive information like passwords
2. **Access Control**: Secure Redis instance in production
3. **Data Encryption**: Consider encryption for sensitive cached data

## Production Considerations

### Deployment
1. **Cache Warming**: Consider pre-loading critical data after deployment
2. **Gradual Rollout**: Test cache behavior in staging environment
3. **Monitoring**: Set up comprehensive cache monitoring

### Scaling
1. **Redis Cluster**: Consider Redis cluster for high availability
2. **Connection Pooling**: Tune connection pool settings for load
3. **Memory Management**: Plan Redis memory requirements

### Backup and Recovery
1. **AOF Persistence**: Enabled by default in our configuration
2. **Regular Backups**: Consider periodic Redis data backups
3. **Disaster Recovery**: Plan for Redis instance recovery

This implementation follows **industry best practices** used by companies like Netflix, Uber, and financial institutions, ensuring reliability, performance, and maintainability.

## 🌍 Language Versions

- 🇺🇸 **English**: You are here!
- 🇧🇷 **Português**: [Configuração Redis em Português](../pt/redis-cache-setup-pt.md)

---

*For more information, see the [main project documentation](../../../README.md).*
