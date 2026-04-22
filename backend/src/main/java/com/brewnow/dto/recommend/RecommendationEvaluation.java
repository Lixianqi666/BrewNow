package com.brewnow.dto.recommend;

import lombok.Data;

@Data
public class RecommendationEvaluation {

    private int topK;
    private double lambda;
    private String season;
    private RecommendationMetrics baseline;
    private RecommendationMetrics timeDecay;
    private RecommendationMetrics seasonAware;
}
