package com.brewnow.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    public static final String HOME_RECOMMEND_CACHE = "recommend:home";
    public static final String RELATED_RECOMMEND_CACHE = "recommend:related";
    public static final String HOT_PRODUCTS_CACHE = "product:hot";
    public static final String CATEGORY_CACHE = "product:categories";
    public static final String RECOMMEND_STATS_CACHE = "recommend:stats";
    public static final String RECOMMEND_EVAL_CACHE = "recommend:evaluation";

    @Bean
    public CacheManager cacheManager(
            @Value("${app.cache.redis-enabled:false}") boolean redisEnabled,
            @Value("${app.cache.default-ttl-minutes:30}") long defaultTtlMinutes,
            @Value("${app.cache.home-recommend-ttl-minutes:10}") long homeTtlMinutes,
            @Value("${app.cache.related-recommend-ttl-minutes:30}") long relatedTtlMinutes,
            @Value("${app.cache.hot-products-ttl-minutes:30}") long hotTtlMinutes,
            @Value("${app.cache.category-ttl-minutes:60}") long categoryTtlMinutes,
            @Value("${app.cache.metrics-ttl-minutes:15}") long metricsTtlMinutes,
            ObjectProvider<RedisConnectionFactory> redisConnectionFactoryProvider) {
        RedisConnectionFactory redisConnectionFactory = redisConnectionFactoryProvider.getIfAvailable();
        if (!redisEnabled || redisConnectionFactory == null) {
            log.info("Cache backend: ConcurrentMapCacheManager (redis-enabled={}, redis-available={})",
                    redisEnabled, redisConnectionFactory != null);
            return new ConcurrentMapCacheManager(
                    HOME_RECOMMEND_CACHE,
                    RELATED_RECOMMEND_CACHE,
                    HOT_PRODUCTS_CACHE,
                    CATEGORY_CACHE,
                    RECOMMEND_STATS_CACHE,
                    RECOMMEND_EVAL_CACHE
            );
        }

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .entryTtl(Duration.ofMinutes(defaultTtlMinutes))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put(HOME_RECOMMEND_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(homeTtlMinutes)));
        cacheConfigurations.put(RELATED_RECOMMEND_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(relatedTtlMinutes)));
        cacheConfigurations.put(HOT_PRODUCTS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(hotTtlMinutes)));
        cacheConfigurations.put(CATEGORY_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(categoryTtlMinutes)));
        cacheConfigurations.put(RECOMMEND_STATS_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(metricsTtlMinutes)));
        cacheConfigurations.put(RECOMMEND_EVAL_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(metricsTtlMinutes)));

        log.info("Cache backend: RedisCacheManager (default-ttl={}m, home={}m, related={}m, metrics={}m)",
                defaultTtlMinutes, homeTtlMinutes, relatedTtlMinutes, metricsTtlMinutes);
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
