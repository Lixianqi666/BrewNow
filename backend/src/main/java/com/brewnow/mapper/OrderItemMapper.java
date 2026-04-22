package com.brewnow.mapper;

import com.brewnow.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单项Mapper接口
 */
@Mapper
public interface OrderItemMapper {

    /**
     * 根据ID查询订单项
     */
    OrderItem selectById(@Param("orderItemId") Integer orderItemId);

    /**
     * 根据订单ID查询订单项列表
     */
    List<OrderItem> selectByOrderId(@Param("orderId") Integer orderId);

    /**
     * 根据商品ID查询订单项列表
     */
    List<OrderItem> selectByProductId(@Param("productId") Integer productId);

    /**
     * 查询用户对某商品最新可评价订单项
     */
    OrderItem selectLatestReviewableByUserAndProduct(@Param("userId") Integer userId,
                                                     @Param("productId") Integer productId);

    /**
     * 查询所有订单项
     */
    List<OrderItem> selectAll();

    /**
     * 分页查询订单项
     */
    List<OrderItem> selectByPage(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 插入订单项
     */
    Integer insert(OrderItem orderItem);

    /**
     * 根据ID更新订单项
     */
    Integer updateById(OrderItem orderItem);

    /**
     * 根据ID删除订单项
     */
    Integer deleteById(@Param("orderItemId") Integer orderItemId);

    /**
     * 根据订单ID删除订单项
     */
    Integer deleteByOrderId(@Param("orderId") Integer orderId);

    /**
     * 统计订单项总数
     */
    Integer countAll();

    /**
     * 根据订单ID统计订单项数量
     */
    Integer countByOrderId(@Param("orderId") Integer orderId);
}
