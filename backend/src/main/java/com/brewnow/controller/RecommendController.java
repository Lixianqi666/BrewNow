package com.brewnow.controller;

import com.brewnow.common.Result;
import com.brewnow.dto.recommend.RecommendationEvaluation;
import com.brewnow.dto.recommend.RecommendationItem;
import com.brewnow.dto.recommend.RecommendationStats;
import com.brewnow.entity.Product;
import com.brewnow.service.RecommendationService;
import com.brewnow.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recommend")
@Tag(name = "推荐模块", description = "协同过滤、时间衰减、季节增强与评估相关接口")
public class RecommendController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/home")
    @Operation(summary = "首页推荐商品", description = "返回首页推荐商品列表，已融合协同过滤、时间衰减与季节增强策略")
    public Result<List<Product>> getHomeRecommendations(
            @Parameter(description = "Bearer Token，可选；登录后可返回个性化推荐")
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "返回数量")
            @RequestParam(defaultValue = "8") Integer limit) {
        return Result.success(recommendationService.getHomeRecommendations(resolveUserId(authHeader), limit));
    }

    @GetMapping("/home/explain")
    @Operation(summary = "首页推荐解释", description = "返回带推荐理由的首页推荐结果")
    public Result<List<RecommendationItem>> getHomeRecommendationItems(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "8") Integer limit) {
        return Result.success(recommendationService.getHomeRecommendationItems(resolveUserId(authHeader), limit));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "相关推荐商品", description = "基于当前商品生成相关推荐列表")
    public Result<List<Product>> getRelatedProducts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "6") Integer limit) {
        return Result.success(recommendationService.getRelatedProducts(resolveUserId(authHeader), productId, limit));
    }

    @GetMapping("/product/{productId}/explain")
    @Operation(summary = "相关推荐解释", description = "返回带推荐理由的相关推荐结果")
    public Result<List<RecommendationItem>> getRelatedRecommendationItems(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "6") Integer limit) {
        return Result.success(recommendationService.getRelatedRecommendationItems(resolveUserId(authHeader), productId, limit));
    }

    @GetMapping("/stats")
    @Operation(summary = "推荐统计", description = "返回行为规模、推荐覆盖率和评估摘要，适合产品演示与运营分析")
    public Result<RecommendationStats> getRecommendationStats(@RequestParam(defaultValue = "10") Integer topK) {
        return Result.success(recommendationService.getRecommendationStats(topK));
    }

    @GetMapping("/evaluation")
    @Operation(summary = "推荐效果评估", description = "对比基线协同过滤、时间衰减协同过滤和季节增强协同过滤效果")
    public Result<RecommendationEvaluation> getRecommendationEvaluation(@RequestParam(defaultValue = "10") Integer topK) {
        return Result.success(recommendationService.evaluateRecommendationQuality(topK));
    }

    private Integer resolveUserId(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return null;
            }
            return jwtUtil.getUserIdFromToken(authHeader.substring(7));
        } catch (Exception e) {
            return null;
        }
    }
}
