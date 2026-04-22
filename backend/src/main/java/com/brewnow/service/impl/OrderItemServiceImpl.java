package com.brewnow.service.impl;

import com.brewnow.entity.OrderItem;
import com.brewnow.mapper.OrderItemMapper;
import com.brewnow.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 订单项服务实现类
 */
@Service
public class OrderItemServiceImpl implements OrderItemService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public List<OrderItem> getOrderItemsByOrderId(Integer orderId) {
        return orderItemMapper.selectByOrderId(orderId);
    }

    @Override
    public OrderItem getOrderItemById(Integer orderItemId) {
        return orderItemMapper.selectById(orderItemId);
    }

    @Override
    @Transactional
    public boolean createOrderItem(OrderItem orderItem) {
        return orderItemMapper.insert(orderItem) > 0;
    }

    @Override
    @Transactional
    public boolean updateOrderItem(OrderItem orderItem) {
        return orderItemMapper.updateById(orderItem) > 0;
    }

    @Override
    @Transactional
    public boolean deleteOrderItem(Integer orderItemId) {
        return orderItemMapper.deleteById(orderItemId) > 0;
    }

    @Override
    @Transactional
    public boolean createOrderItems(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return false;
        }

        for (OrderItem orderItem : orderItems) {
            if (orderItemMapper.insert(orderItem) <= 0) {
                return false;
            }
        }
        return true;
    }
}