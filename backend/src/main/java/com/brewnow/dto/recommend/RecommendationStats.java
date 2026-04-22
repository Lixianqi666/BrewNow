package com.brewnow.dto.recommend;

import com.brewnow.entity.UserBehavior;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RecommendationStats {

    private int totalBehaviors;
    private int activeUsers;
    private int recommendableUsers;
    private int activeProducts;
    private int totalFavorites;
    private Map<String, Integer> behaviorTypeCounts;
    private List<UserBehavior> recentBehaviors;
    private RecommendationEvaluation evaluation;
}
