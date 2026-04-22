package com.brewnow.service;

import com.brewnow.entity.CartItem;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {

    /**
     * 添加商品到购物车
     * 
     * @param userId    用户ID
     * @param productId 商品ID
     * @param quantity  数量
     * @return 是否添加成功
     */
    boolean addToCart(Integer userId, Integer productId, Integer quantity);

    /**
     * 获取用户购物车中的所有商品
     * 
     * @param userId 用户ID
     * @return 购物车商品列表
     */
    List<CartItem> getCartItems(Integer userId);

    /**
     * 更新购物车中商品数量
     * 
     * @param userId     用户ID
     * @param cartItemId 购物车项ID
     * @param quantity   新数量
     * @return 是否更新成功
     */
    boolean updateCartItemQuantity(Integer userId, Integer cartItemId, Integer quantity);

    /**
     * 从购物车中删除商品
     * 
     * @param userId     用户ID
     * @param cartItemId 购物车项ID
     * @return 是否删除成功
     */
    boolean removeFromCart(Integer userId, Integer cartItemId);

    /**
     * 清空用户购物车
     * 
     * @param userId 用户ID
     * @return 是否清空成功
     */
    boolean clearCart(Integer userId);

    /**
     * 获取用户购物车中商品总数量
     * 
     * @param userId 用户ID
     * @return 商品总数量
     */
    Integer getCartItemCount(Integer userId);
}