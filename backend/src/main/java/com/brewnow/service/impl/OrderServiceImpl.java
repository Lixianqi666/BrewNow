package com.brewnow.service.impl;

import com.brewnow.entity.Order;
import com.brewnow.entity.OrderItem;
import com.brewnow.entity.CartItem;
import com.brewnow.entity.Product;
import com.brewnow.enums.OrderStatus;
import com.brewnow.enums.PaymentMethod;
import com.brewnow.dto.order.MerchantOrderSummary;
import com.brewnow.mapper.OrderMapper;
import com.brewnow.mapper.OrderItemMapper;
import com.brewnow.service.BehaviorService;
import com.brewnow.service.OrderService;
import com.brewnow.service.CartService;
import com.brewnow.service.ProductService;
import com.brewnow.service.RecommendationCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private BehaviorService behaviorService;

    @Autowired
    private RecommendationCacheService recommendationCacheService;

    @Override
    public Order getOrderById(Integer orderId) {
        return orderMapper.selectById(orderId);
    }

    @Override
    public Order getOrderByOrderNumber(String orderNumber) {
        return orderMapper.selectByOrderNumber(orderNumber);
    }

    @Override
    public List<Order> getOrdersByUserId(Integer userId) {
        return orderMapper.selectByUserId(userId);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderMapper.selectAll();
    }

    @Override
    public List<Order> getOrdersByPage(int page, int size) {
        int offset = (page - 1) * size;
        return orderMapper.selectByPage(offset, size);
    }

    @Override
    public List<Order> getOrdersByStatusWithPage(OrderStatus status, int page, int size) {
        int offset = (page - 1) * size;
        return orderMapper.selectByStatusWithPage(status, offset, size);
    }

    @Override
    @Transactional
    public boolean createOrder(Order order) {
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.PENDING);
        return orderMapper.insert(order) > 0;
    }

    @Override
    @Transactional
    public boolean updateOrderStatus(Integer orderId, OrderStatus status) {
        Order order = getOrderById(orderId);
        if (order == null) {
            return false;
        }

        order.setOrderStatus(status);
        return orderMapper.updateById(order) > 0;
    }

    @Override
    @Transactional
    public boolean cancelOrder(Integer orderId) {
        return updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }

    @Override
    @Transactional
    public boolean adminCancelOrder(Integer orderId) {
        Order order = getOrderById(orderId);
        if (order == null) {
            return false;
        }

        if (order.getOrderStatus() != OrderStatus.PENDING && order.getOrderStatus() != OrderStatus.PAID) {
            return false;
        }

        if (order.getOrderStatus() == OrderStatus.PAID && order.getStockDeducted() != null && order.getStockDeducted() == 1) {
            List<OrderItem> orderItems = orderItemMapper.selectByOrderId(orderId);
            for (OrderItem orderItem : orderItems) {
                boolean restored = productService.restoreStock(orderItem.getProductId(), orderItem.getQuantity());
                if (!restored) {
                    throw new RuntimeException("回补库存失败：" + orderItem.getProductNameSnapshot());
                }
            }
            order.setStockDeducted(0);
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        return orderMapper.updateById(order) > 0;
    }

    @Override
    @Transactional
    public boolean deleteOrder(Integer orderId) {
        Order order = getOrderById(orderId);
        if (order == null) {
            return false;
        }

        order.setDeletedAt(LocalDateTime.now());
        return orderMapper.updateById(order) > 0;
    }

    @Override
    public Integer getOrderCount() {
        return orderMapper.countAll();
    }

    @Override
    public Integer getOrderCountByStatus(OrderStatus status) {
        return orderMapper.countByStatus(status);
    }

    // =============== 商家专用方法实现 ===============

    @Override
    public Integer getOrderCountByMerchant(String merchantId) {
        return orderMapper.countByMerchant(merchantId);
    }

    // =============== 新方法实现 ===============

    @Override
    @Transactional
    public Order createOrderFromCart(Integer userId, String shippingAddress, String contactPhone,
            String paymentMethod, String remark) {
        return createOrderFromCart(userId, shippingAddress, contactPhone, paymentMethod, remark, null);
    }

    @Override
    @Transactional
    public Order createOrderFromCart(Integer userId, String shippingAddress, String contactPhone,
            String paymentMethod, String remark, List<CartItem> checkoutItems) {
        try {
            // 获取本次结算商品（支持购物车勾选结算和立即购买）
            List<CartItem> cartItems = resolveCheckoutItems(userId, checkoutItems);
            if (cartItems == null || cartItems.isEmpty()) {
                throw new RuntimeException("购物车为空");
            }

            // 生成订单号
            String orderNumber = generateOrderNumber();

            // 计算订单总金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (CartItem cartItem : cartItems) {
                Product product = productService.getProductById(cartItem.getProductId());
                if (product == null) {
                    throw new RuntimeException("商品不存在：" + cartItem.getProductId());
                }
                if (cartItem.getQuantity() == null || cartItem.getQuantity() <= 0) {
                    throw new RuntimeException("商品数量错误：" + cartItem.getProductId());
                }
                if (product.getStockQuantity() != null && product.getStockQuantity() < cartItem.getQuantity()) {
                    throw new RuntimeException("商品库存不足：" + product.getProductName());
                }
                BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);
            }

            // 创建订单
            Order order = new Order();
            order.setUserId(userId);
            order.setOrderNumber(orderNumber);
            order.setTotalAmount(totalAmount);
            order.setOrderStatus(OrderStatus.PENDING);
            order.setShippingAddress(shippingAddress);
            order.setContactPhone(contactPhone);
            order.setRemark(remark);
            order.setOrderDate(LocalDateTime.now());
            order.setStockDeducted(0);

            // 设置支付方式
            if (paymentMethod != null) {
                try {
                    order.setPaymentMethod(PaymentMethod.valueOf(paymentMethod.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    order.setPaymentMethod(PaymentMethod.ALIPAY); // 默认支付宝
                }
            }

            // 插入订单
            if (orderMapper.insert(order) <= 0) {
                throw new RuntimeException("创建订单失败");
            }

            // 创建订单项
            for (CartItem cartItem : cartItems) {
                Product product = productService.getProductById(cartItem.getProductId());

                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(order.getOrderId());
                orderItem.setProductId(cartItem.getProductId());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setUnitPrice(product.getPrice());
                orderItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
                orderItem.setProductNameSnapshot(product.getProductName());
                orderItem.setBrandSnapshot(product.getBrand());
                orderItem.setCategorySnapshot(product.getCategory());
                orderItem.setImageUrlSnapshot(product.getImageUrl());

                if (orderItemMapper.insert(orderItem) <= 0) {
                    throw new RuntimeException("创建订单项失败");
                }
            }

            // 仅清理本次已结算的购物车商品；立即购买不动购物车
            clearCheckedOutCartItems(userId, checkoutItems, cartItems);

            return order;
        } catch (Exception e) {
            throw new RuntimeException("创建订单失败：" + e.getMessage());
        }
    }

    @Override
    public List<Order> getOrdersByUserIdWithPage(Integer userId, int page, int size) {
        int offset = (page - 1) * size;
        return orderMapper.selectByUserIdWithPage(userId, offset, size);
    }

    @Override
    public List<Order> getOrdersByUserIdAndStatus(Integer userId, OrderStatus status, int page, int size) {
        int offset = (page - 1) * size;
        return orderMapper.selectByUserIdAndStatus(userId, status, offset, size);
    }

    @Override
    public Map<String, Object> getUserOrderStats(Integer userId) {
        Map<String, Object> stats = new HashMap<>();

        // 总订单数
        int totalOrders = orderMapper.countByUserId(userId);
        stats.put("totalOrders", totalOrders);

        // 各状态订单数
        stats.put("pendingOrders", orderMapper.countByUserIdAndStatus(userId, OrderStatus.PENDING));
        stats.put("paidOrders", orderMapper.countByUserIdAndStatus(userId, OrderStatus.PAID));
        stats.put("shippedOrders", orderMapper.countByUserIdAndStatus(userId, OrderStatus.SHIPPED));
        stats.put("deliveredOrders", orderMapper.countByUserIdAndStatus(userId, OrderStatus.DELIVERED));
        stats.put("cancelledOrders", orderMapper.countByUserIdAndStatus(userId, OrderStatus.CANCELLED));

        // 总消费金额
        BigDecimal totalAmount = orderMapper.getTotalAmountByUserId(userId);
        stats.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);

        return stats;
    }

    @Override
    @Transactional
    public boolean payOrder(Integer orderId, Integer userId) {
        Order order = getOrderById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return false;
        }
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            return false;
        }

        if (order.getStockDeducted() == null || order.getStockDeducted() == 0) {
            List<OrderItem> orderItems = orderItemMapper.selectByOrderId(orderId);
            for (OrderItem orderItem : orderItems) {
                boolean deducted = productService.deductStock(orderItem.getProductId(), orderItem.getQuantity());
                if (!deducted) {
                    throw new RuntimeException("商品库存不足：" + orderItem.getProductNameSnapshot());
                }
                behaviorService.recordBehavior(userId, orderItem.getProductId(), "PURCHASE");
                recommendationCacheService.evictRecommendationCaches(userId, orderItem.getProductId());
            }
            order.setStockDeducted(1);
        }

        order.setOrderStatus(OrderStatus.PAID);
        return orderMapper.updateById(order) > 0;
    }

    @Override
    public long getTotalOrderCount() {
        Integer count = orderMapper.countAll();
        return count != null ? count.longValue() : 0L;
    }

    @Override
    public long getTodayNewOrderCount() {
        Integer count = orderMapper.countTodayNewOrders();
        return count != null ? count.longValue() : 0L;
    }

    @Override
    public List<MerchantOrderSummary> getOrdersByMerchant(String merchantId, String status, int page, int size) {
        int offset = (page - 1) * size;
        return orderMapper.selectByMerchant(merchantId, status, offset, size);
    }

    @Override
    public Integer getOrderCountByMerchant(String merchantId, String status) {
        return orderMapper.countByMerchantWithStatus(merchantId, status);
    }

    private List<CartItem> resolveCheckoutItems(Integer userId, List<CartItem> checkoutItems) {
        List<CartItem> userCartItems = cartService.getCartItems(userId);
        if (checkoutItems == null || checkoutItems.isEmpty()) {
            return userCartItems;
        }

        Map<Integer, CartItem> cartItemMap = userCartItems.stream()
                .filter(item -> item.getCartItemId() != null)
                .collect(Collectors.toMap(CartItem::getCartItemId, item -> item, (first, second) -> first));

        List<CartItem> resolvedItems = new ArrayList<>();
        for (CartItem requestItem : checkoutItems) {
            if (requestItem == null) {
                continue;
            }

            Integer cartItemId = requestItem.getCartItemId();
            if (cartItemId != null && cartItemId > 0) {
                CartItem actualCartItem = cartItemMap.get(cartItemId);
                if (actualCartItem == null) {
                    throw new RuntimeException("选中的购物车商品不存在");
                }
                resolvedItems.add(actualCartItem);
                continue;
            }

            Integer productId = requestItem.getProductId();
            Integer quantity = requestItem.getQuantity();
            if (productId == null || quantity == null || quantity <= 0) {
                throw new RuntimeException("结算商品参数错误");
            }

            CartItem directItem = new CartItem();
            directItem.setProductId(productId);
            directItem.setQuantity(quantity);
            resolvedItems.add(directItem);
        }

        return resolvedItems;
    }

    private void clearCheckedOutCartItems(Integer userId, List<CartItem> checkoutItems, List<CartItem> resolvedItems) {
        if (checkoutItems == null || checkoutItems.isEmpty()) {
            cartService.clearCart(userId);
            return;
        }

        resolvedItems.stream()
                .map(CartItem::getCartItemId)
                .filter(cartItemId -> cartItemId != null && cartItemId > 0)
                .forEach(cartItemId -> cartService.removeFromCart(userId, cartItemId));
    }

    /**
     * 生成订单号
     */
    private String generateOrderNumber() {
        // 格式：ORD + 年月日 + 时分秒 + 3位随机数
        LocalDateTime now = LocalDateTime.now();
        String datePart = String.format("%04d%02d%02d%02d%02d%02d",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                now.getHour(), now.getMinute(), now.getSecond());
        int random = (int) (Math.random() * 1000);
        return "ORD" + datePart + String.format("%03d", random);
    }
}
