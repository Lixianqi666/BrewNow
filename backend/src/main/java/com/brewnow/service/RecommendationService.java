package com.brewnow.service;

import com.brewnow.dto.recommend.RecommendationEvaluation;
import com.brewnow.dto.recommend.RecommendationItem;
import com.brewnow.dto.recommend.RecommendationStats;
import com.brewnow.entity.Product;

import java.util.List;

public interface RecommendationService {

    List<Product> getHomeRecommendations(Integer userId, int limit);

    List<Product> getRelatedProducts(Integer userId, Integer productId, int limit);

    List<RecommendationItem> getHomeRecommendationItems(Integer userId, int limit);

    List<RecommendationItem> getRelatedRecommendationItems(Integer userId, Integer productId, int limit);

    RecommendationStats getRecommendationStats(int topK);

    RecommendationEvaluation evaluateRecommendationQuality(int topK);
}
