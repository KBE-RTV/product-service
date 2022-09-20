package com.kbertv.productService.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Configuration for Redis Cache
 */
@EnableCaching
@Configuration
public class RedisCacheConfig {
    /**
     * Creates a RedisCacheManagerBuilderCustomizer with two Caches, one for products and one for components, each with a TTL of 10 min.
     *
     * @return RedisCacheManagerBuilderCustomizer
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> builder
                .withCacheConfiguration("productCache",
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration("componentCache",
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)));
    }

    /**
     * Creates a Cache configuration with a TTL of 60 min, disabled Null Values and GenericJackson2JsonRedisSerializer as serializer.
     *
     * @return RedisCacheConfiguration
     */
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}
