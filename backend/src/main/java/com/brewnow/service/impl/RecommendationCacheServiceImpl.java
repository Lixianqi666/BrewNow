package com.brewnow.service.impl;

import com.brewnow.config.CacheConfig;
import com.brewnow.service.RecommendationCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class RecommendationCacheServiceImpl implements RecommendationCacheService {

    @Autowired
    private CacheManager cacheManager;

    @Override
    public void evictRecommendationCaches(Integer userId, Integer productId) {
        clearCache(CacheConfig.HOME_RECOMMEND_CACHE);
        clearCache(CacheConfig.RELATED_RECOMMEND_CACHE);
        clearCache(CacheConfig.RECOMMEND_STATS_CACHE);
        clearCache(CacheConfig.RECOMMEND_EVAL_CACHE);
    }

    @Override
    public void evictHotAndCategoryCaches() {
        clearCache(CacheConfig.HOT_PRODUCTS_CACHE);
        clearCache(CacheConfig.CATEGORY_CACHE);
    }

    private void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (Objects.nonNull(cache)) {
            cache.clear();
        }
    }
}
