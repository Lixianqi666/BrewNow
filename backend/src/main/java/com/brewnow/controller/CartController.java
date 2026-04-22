package com.brewnow.controller;

import com.brewnow.common.Result;
import com.brewnow.entity.CartItem;
import com.brewnow.service.BehaviorService;
import com.brewnow.service.CartService;
import com.brewnow.service.RecommendationCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 购物车控制器
 */
@RestController
@RequestMapping("/cart")
@Tag(name = "购物车模块", description = "购物车商品增删改查")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private BehaviorService behaviorService;

    @Autowired
    private RecommendationCacheService recommendationCacheService;

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    @Operation(summary = "加入购物车", description = "将商品加入当前用户购物车，并记录加购行为")
    public Result<Void> addToCart(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            // 从JWT中获取用户ID
            Integer userId = (Integer) httpRequest.getAttribute("userId");
            Integer productId = (Integer) request.get("productId");
            Integer quantity = (Integer) request.get("quantity");

            if (productId == null || quantity == null || quantity <= 0) {
                return Result.error("参数错误");
            }

            boolean success = cartService.addToCart(userId, productId, quantity);
            if (success) {
                behaviorService.recordBehavior(userId, productId, "CART");
                recommendationCacheService.evictRecommendationCaches(userId, productId);
                return Result.success("商品已添加到购物车", null);
            } else {
                return Result.error("添加到购物车失败");
            }
        } catch (Exception e) {
            return Result.error("添加到购物车失败");
        }
    }

    /**
     * 获取用户购物车商品列表
     */
    @GetMapping("/list")
    @Operation(summary = "购物车列表", description = "查询当前用户购物车商品明细")
    public Result<List<CartItem>> getCartItems(HttpServletRequest request) {
        try {
            // 从JWT中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            List<CartItem> cartItems = cartService.getCartItems(userId);
            return Result.success(cartItems);
        } catch (Exception e) {
            return Result.error("获取购物车失败");
        }
    }

    /**
     * 更新购物车商品数量
     */
    @PutMapping("/update")
    @Operation(summary = "更新购物车数量", description = "更新购物车中指定商品数量")
    public Result<Void> updateCartItemQuantity(@RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        try {
            // 从JWT中获取用户ID
            Integer userId = (Integer) httpRequest.getAttribute("userId");
            Integer cartItemId = (Integer) request.get("cartItemId");
            Integer quantity = (Integer) request.get("quantity");

            if (cartItemId == null || quantity == null || quantity < 0) {
                return Result.error("参数错误");
            }

            boolean success = cartService.updateCartItemQuantity(userId, cartItemId, quantity);
            if (success) {
                return Result.success("购物车更新成功", null);
            } else {
                return Result.error("购物车更新失败");
            }
        } catch (Exception e) {
            return Result.error("购物车更新失败");
        }
    }

    /**
     * 从购物车删除商品
     */
    @DeleteMapping("/remove")
    @Operation(summary = "移除购物车商品", description = "从购物车删除指定商品")
    public Result<Void> removeFromCart(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            // 从JWT中获取用户ID
            Integer userId = (Integer) httpRequest.getAttribute("userId");
            Integer cartItemId = (Integer) request.get("cartItemId");

            if (cartItemId == null) {
                return Result.error("参数错误");
            }

            boolean success = cartService.removeFromCart(userId, cartItemId);
            if (success) {
                return Result.success("商品已从购物车移除", null);
            } else {
                return Result.error("移除商品失败");
            }
        } catch (Exception e) {
            return Result.error("移除商品失败");
        }
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空购物车", description = "清空当前用户购物车")
    public Result<Void> clearCart(HttpServletRequest request) {
        try {
            // 从JWT中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            boolean success = cartService.clearCart(userId);
            if (success) {
                return Result.success("购物车已清空", null);
            } else {
                return Result.error("清空购物车失败");
            }
        } catch (Exception e) {
            return Result.error("清空购物车失败");
        }
    }

    /**
     * 获取购物车商品数量
     */
    @GetMapping("/count")
    @Operation(summary = "购物车数量", description = "获取当前用户购物车商品总数")
    public Result<Integer> getCartItemCount(HttpServletRequest request) {
        try {
            // 从JWT中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            Integer count = cartService.getCartItemCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("获取购物车数量失败");
        }
    }
}
