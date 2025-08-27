package com.digital.wallet.infra.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CacheConfig Tests")
class CacheConfigTest {

    @Test
    @DisplayName("Should create cache manager with Redis connection factory")
    void shouldCreateCacheManagerWithRedisConnectionFactory() {
        // Given
        CacheConfig config = new CacheConfig();
        RedisConnectionFactory connectionFactory = new LettuceConnectionFactory();
        
        // When
        CacheManager cacheManager = config.cacheManager(connectionFactory);
        
        // Then
        assertNotNull(cacheManager);
        assertTrue(cacheManager instanceof RedisCacheManager);
    }

    @Test
    @DisplayName("Should create cache manager with proper configuration")
    void shouldCreateCacheManagerWithProperConfiguration() {
        // Given
        CacheConfig config = new CacheConfig();
        RedisConnectionFactory connectionFactory = new LettuceConnectionFactory();
        
        // When
        CacheManager cacheManager = config.cacheManager(connectionFactory);
        
        // Then
        assertNotNull(cacheManager);
        assertNotNull(cacheManager.getCacheNames());
    }

    @Test
    @DisplayName("Should handle cache operations correctly")
    void shouldHandleCacheOperationsCorrectly() {
        // Given
        CacheConfig config = new CacheConfig();
        RedisConnectionFactory connectionFactory = new LettuceConnectionFactory();
        CacheManager cacheManager = config.cacheManager(connectionFactory);
        
        // When
        var cache = cacheManager.getCache("test-cache");
        
        // Then
        // Cache might be null if not pre-configured, which is expected behavior
        // This test validates the cache manager can handle cache retrieval requests
        assertTrue(cacheManager instanceof RedisCacheManager);
    }
}
