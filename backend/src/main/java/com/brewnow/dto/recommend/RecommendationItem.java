package com.brewnow.dto.recommend;

import com.brewnow.entity.Product;
import lombok.Data;

@Data
public class RecommendationItem {

    private Product product;
    private double score;
    private String strategy;
    private String reason;
}
