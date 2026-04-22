package com.brewnow.mapper;

import com.brewnow.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 购物车Mapper接口
 */
@Mapper
public interface CartMapper {

    /**
     * 根据用户ID查询购物车
     */
    Cart selectByUserId(@Param("userId") Integer userId);

    /**
     * 创建购物车
     */
    Integer insert(Cart cart);

    /**
     * 根据购物车ID查询购物车
     */
    Cart selectById(@Param("cartId") Integer cartId);

    /**
     * 更新购物车
     */
    Integer updateById(Cart cart);

    /**
     * 删除购物车
     */
    Integer deleteById(@Param("cartId") Integer cartId);
}