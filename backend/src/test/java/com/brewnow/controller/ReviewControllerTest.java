package com.brewnow.controller;

import com.brewnow.common.Result;
import com.brewnow.entity.OrderItem;
import com.brewnow.entity.ProductReview;
import com.brewnow.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    @Test
    void getProductReviewsShouldReturnReviewList() {
        ProductReview review = new ProductReview();
        review.setReviewId(1L);
        review.setProductId(9);
        review.setRating(5);
        review.setContent("香气高扬，回甘明显");

        when(reviewService.getProductReviews(9)).thenReturn(List.of(review));

        Result<List<ProductReview>> result = reviewController.getProductReviews(9);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals(5, result.getData().get(0).getRating());
    }

    @Test
    void getReviewableOrderItemShouldReturnOrderItemWhenAvailable() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("userId")).thenReturn(12);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrderItemId(101);
        orderItem.setProductId(8);

        when(reviewService.getLatestReviewableOrderItem(12, 8)).thenReturn(orderItem);

        Result<Map<String, Object>> result = reviewController.getReviewableOrderItem(8, request);

        assertEquals(200, result.getCode());
        assertEquals(true, result.getData().get("canReview"));
        assertEquals(orderItem, result.getData().get("orderItem"));
    }

    @Test
    void submitReviewShouldReturnSuccessWhenServiceAccepts() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("userId")).thenReturn(12);

        ProductReview review = new ProductReview();
        review.setOrderItemId(101);
        review.setRating(5);
        review.setContent("茶汤醇厚");

        when(reviewService.submitReview(12, review)).thenReturn(true);

        Result<Void> result = reviewController.submitReview(review, request);

        assertEquals(200, result.getCode());
        assertTrue(result.getMessage().contains("成功"));
    }
}
