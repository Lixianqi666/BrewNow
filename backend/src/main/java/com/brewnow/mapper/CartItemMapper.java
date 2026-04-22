package com.brewnow.mapper;

import com.brewnow.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 购物车项Mapper接口
 */
@Mapper
public interface CartItemMapper {

    /**
     * 根据购物车ID查询所有购物车项
     */
    List<CartItem> selectByCartId(@Param("cartId") Integer cartId);

    /**
     * 根据购物车ID和商品ID查询购物车项
     */
    CartItem selectByCartIdAndProductId(@Param("cartId") Integer cartId, @Param("productId") Integer productId);

    /**
     * 添加购物车项
     */
    Integer insert(CartItem cartItem);

    /**
     * 更新购物车项数量
     */
    Integer updateQuantity(@Param("cartItemId") Integer cartItemId, @Param("quantity") Integer quantity);

    /**
     * 删除购物车项
     */
    Integer deleteById(@Param("cartItemId") Integer cartItemId);

    /**
     * 根据购物车ID删除所有购物车项（清空购物车）
     */
    Integer deleteByCartId(@Param("cartId") Integer cartId);

    /**
     * 统计购物车中商品数量
     */
    Integer countByCartId(@Param("cartId") Integer cartId);
}