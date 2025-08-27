package com.digital.wallet.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration for distributed caching following industry best practices
 * 
 * This configuration enables distributed caching using Redis as the cache provider.
 * It follows industry standards for:
 * - Hierarchical key naming (namespace:entity:operation:version)
 * - Appropriate TTLs for financial data
 * - Consistent cache region naming
 * - JDK serialization for reliability
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${spring.application.name}")
    private String applicationName;
    
    @Value("${app.cache.version}")
    private String cacheVersion;
    
    // Cache TTL configuration from environment variables (in minutes/seconds)
    @Value("${app.cache.ttl.default}")
    private int defaultTtlMinutes;
    
    @Value("${app.cache.ttl.wallet-list}")
    private int walletListTtlMinutes;
    
    @Value("${app.cache.ttl.wallet-single}")
    private int walletSingleTtlMinutes;
    
    @Value("${app.cache.ttl.wallet-balance}")
    private int walletBalanceSeconds;
    
    @Value("${app.cache.ttl.wallet-transactions}")
    private int walletTransactionsTtlMinutes;
    
    @Value("${app.cache.ttl.user-profile}")
    private int userProfileTtlMinutes;

    /**
     * Configure Redis Cache Manager with industry best practices
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Use JDK serialization for reliable serialization/deserialization
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
        
        // Wallet list cache - configurable TTL for financial data consistency
        cacheConfigurations.put("wallet-list", defaultConfig.entryTtl(Duration.ofMinutes(walletListTtlMinutes)));
        
        // Individual wallet cache - configurable TTL for balance accuracy
        cacheConfigurations.put("wallet-single", defaultConfig.entryTtl(Duration.ofMinutes(walletSingleTtlMinutes)));
        
        // Balance cache - configurable TTL for critical financial data
        cacheConfigurations.put("wallet-balance", defaultConfig.entryTtl(Duration.ofSeconds(walletBalanceSeconds)));
        
        // Transaction history cache - configurable TTL as historical data changes less
        cacheConfigurations.put("wallet-transactions", defaultConfig.entryTtl(Duration.ofMinutes(walletTransactionsTtlMinutes)));
        
        // User profile cache - configurable TTL for relatively static data
        cacheConfigurations.put("user-profile", defaultConfig.entryTtl(Duration.ofMinutes(userProfileTtlMinutes)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
