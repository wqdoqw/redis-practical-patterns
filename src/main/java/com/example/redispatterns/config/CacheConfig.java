package com.example.redispatterns.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {
    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    /**
     * Redis 연결 팩토리가 존재할 때만 CacheManager를 구성합니다.
     */
    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info("Configuring Redis cache manager");

        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration)
                .build();
    }

    /**
     * Cache 연산 중 Redis 장애가 발생해도 비즈니스 로직은 계속 진행되도록 폴백합니다.
     * EX) @Cacheable 조회 시 Redis 연결 오류가 나면 저장소에서 조회해 반환하고, 오류는 로그로만 남깁니다.
     */
    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("[CacheFallback] GET 실패 - cache={}, key={}, reason={}",
                        cache != null ? cache.getName() : "unknown", key, exception.toString());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("[CacheFallback] PUT 실패 - cache={}, key={}, reason={}",
                        cache != null ? cache.getName() : "unknown", key, exception.toString());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("[CacheFallback] EVICT 실패 - cache={}, key={}, reason={}",
                        cache != null ? cache.getName() : "unknown", key, exception.toString());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("[CacheFallback] CLEAR 실패 - cache={}, reason={}",
                        cache != null ? cache.getName() : "unknown", exception.toString());
            }
        };
    }
}
