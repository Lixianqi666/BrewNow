package com.brewnow.service;

public interface RecommendationCacheService {

    void evictRecommendationCaches(Integer userId, Integer productId);

    void evictHotAndCategoryCaches();
}
