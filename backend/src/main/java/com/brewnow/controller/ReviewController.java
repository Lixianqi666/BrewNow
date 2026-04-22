package com.brewnow.controller;

import com.brewnow.common.Result;
import com.brewnow.entity.OrderItem;
import com.brewnow.entity.ProductReview;
import com.brewnow.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/review")
@Tag(name = "评价模块", description = "商品评价查询、可评价校验与提交")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/product/{productId}")
    @Operation(summary = "商品评价列表", description = "查询指定商品的评价列表")
    public Result<List<ProductReview>> getProductReviews(@PathVariable Integer productId) {
        return Result.success(reviewService.getProductReviews(productId));
    }

    @GetMapping("/summary/{productId}")
    @Operation(summary = "商品评价摘要", description = "查询商品平均评分和评价数量")
    public Result<Map<String, Object>> getProductReviewSummary(@PathVariable Integer productId) {
        return Result.success(reviewService.getProductReviewSummary(productId));
    }

    @GetMapping("/can-review/{orderItemId}")
    @Operation(summary = "校验是否可评价", description = "校验当前订单项是否允许用户评价")
    public Result<Map<String, Object>> canReview(@PathVariable Integer orderItemId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();
        result.put("canReview", reviewService.canReview(userId, orderItemId));
        return Result.success(result);
    }

    @GetMapping("/product/{productId}/reviewable")
    @Operation(summary = "查询最近可评价订单项", description = "用于商品详情页快速进入评价")
    public Result<Map<String, Object>> getReviewableOrderItem(@PathVariable Integer productId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        OrderItem orderItem = reviewService.getLatestReviewableOrderItem(userId, productId);
        Map<String, Object> result = new HashMap<>();
        result.put("canReview", orderItem != null);
        result.put("orderItem", orderItem);
        return Result.success(result);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交商品评价", description = "提交订单项评价，已评价订单项不可重复评价")
    public Result<Void> submitReview(@RequestBody ProductReview review, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        boolean success = reviewService.submitReview(userId, review);
        return success ? Result.success("评价成功", null) : Result.error("评价失败");
    }
}
