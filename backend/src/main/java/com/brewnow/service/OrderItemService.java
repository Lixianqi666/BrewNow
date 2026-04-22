package com.brewnow.service;

import com.brewnow.entity.OrderItem;
import java.util.List;

/**
 * 订单项服务接口
 */
public interface OrderItemService {

    /**
     * 根据订单ID查询订单项列表
     */
    List<OrderItem> getOrderItemsByOrderId(Integer orderId);

    /**
     * 根据ID查询订单项
     */
    OrderItem getOrderItemById(Integer orderItemId);

    /**
     * 创建订单项
     */
    boolean createOrderItem(OrderItem orderItem);

    /**
     * 更新订单项
     */
    boolean updateOrderItem(OrderItem orderItem);

    /**
     * 删除订单项
     */
    boolean deleteOrderItem(Integer orderItemId);

    /**
     * 批量创建订单项
     */
    boolean createOrderItems(List<OrderItem> orderItems);
}