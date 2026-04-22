package com.brewnow.service.impl;

import com.brewnow.entity.Order;
import com.brewnow.entity.OrderItem;
import com.brewnow.entity.ProductReview;
import com.brewnow.enums.OrderStatus;
import com.brewnow.mapper.OrderItemMapper;
import com.brewnow.mapper.ProductReviewMapper;
import com.brewnow.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ProductReviewMapper productReviewMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderServiceImpl orderService;

    @Override
    @Transactional
    public boolean submitReview(Integer userId, ProductReview review) {
        if (userId == null || review == null || review.getOrderItemId() == null) {
            throw new RuntimeException("评价参数不完整");
        }
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new RuntimeException("评分范围应为1到5分");
        }
        if (!canReview(userId, review.getOrderItemId())) {
            throw new RuntimeException("当前订单项不可评价");
        }
        if (productReviewMapper.selectByOrderItemId(review.getOrderItemId()) != null) {
            throw new RuntimeException("该商品已评价");
        }

        OrderItem orderItem = orderItemMapper.selectById(review.getOrderItemId());
        review.setUserId(userId);
        review.setOrderId(orderItem.getOrderId());
        review.setProductId(orderItem.getProductId());
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        return productReviewMapper.insert(review) > 0;
    }

    @Override
    public List<ProductReview> getProductReviews(Integer productId) {
        return productReviewMapper.selectByProductId(productId);
    }

    @Override
    public Map<String, Object> getProductReviewSummary(Integer productId) {
        Map<String, Object> summary = productReviewMapper.selectReviewSummary(productId);
        if (summary == null) {
            summary = new HashMap<>();
            summary.put("averageRating", 0);
            summary.put("reviewCount", 0);
        }
        return summary;
    }

    @Override
    public boolean canReview(Integer userId, Integer orderItemId) {
        if (userId == null) {
            return false;
        }
        OrderItem orderItem = orderItemMapper.selectById(orderItemId);
        if (orderItem == null) {
            return false;
        }
        Order order = orderService.getOrderById(orderItem.getOrderId());
        if (order == null || !userId.equals(order.getUserId())) {
            return false;
        }
        if (order.getOrderStatus() != OrderStatus.SHIPPED && order.getOrderStatus() != OrderStatus.DELIVERED) {
            return false;
        }
        return productReviewMapper.selectByOrderItemId(orderItemId) == null;
    }

    @Override
    public OrderItem getLatestReviewableOrderItem(Integer userId, Integer productId) {
        if (userId == null || productId == null) {
            return null;
        }
        return orderItemMapper.selectLatestReviewableByUserAndProduct(userId, productId);
    }
}
