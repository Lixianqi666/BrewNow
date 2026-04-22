package com.brewnow.controller;

import com.brewnow.common.Result;
import com.brewnow.entity.Product;
import com.brewnow.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorite")
@Tag(name = "收藏模块", description = "商品收藏、取消收藏与收藏状态查询")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/toggle")
    @Operation(summary = "切换收藏状态", description = "对指定商品执行收藏或取消收藏")
    public Result<Map<String, Object>> toggleFavorite(@RequestBody Map<String, Integer> payload,
                                                      HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        Integer productId = payload.get("productId");
        if (userId == null || productId == null) {
            return Result.badRequest("参数错误");
        }

        boolean success = favoriteService.toggleFavorite(userId, productId);
        boolean favorite = favoriteService.isFavorite(userId, productId);
        Map<String, Object> result = new HashMap<>();
        result.put("favorited", favorite);
        return success ? Result.success("操作成功", result) : Result.error("收藏操作失败");
    }

    @GetMapping("/list")
    @Operation(summary = "收藏列表", description = "查询当前用户收藏商品列表")
    public Result<List<Product>> getFavorites(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return Result.success(favoriteService.getFavoriteProducts(userId));
    }

    @GetMapping("/status")
    @Operation(summary = "收藏状态", description = "查询当前用户对指定商品的收藏状态")
    public Result<Map<String, Object>> getFavoriteStatus(Integer productId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();
        result.put("favorited", favoriteService.isFavorite(userId, productId));
        return Result.success(result);
    }
}
