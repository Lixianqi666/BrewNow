package com.brewnow.service;

import com.brewnow.entity.OrderItem;
import com.brewnow.entity.ProductReview;

import java.util.List;
import java.util.Map;

public interface ReviewService {

    boolean submitReview(Integer userId, ProductReview review);

    List<ProductReview> getProductReviews(Integer productId);

    Map<String, Object> getProductReviewSummary(Integer productId);

    boolean canReview(Integer userId, Integer orderItemId);

    OrderItem getLatestReviewableOrderItem(Integer userId, Integer productId);
}
