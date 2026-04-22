package com.brewnow.dto.recommend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationMetrics {

    private double precisionAtK;
    private double recallAtK;
    private double hitRateAtK;
    private int evaluatedUsers;
}
