package com.brewnow.service;

import com.brewnow.entity.Order;
import com.brewnow.entity.CartItem;
import com.brewnow.enums.OrderStatus;
import com.brewnow.dto.order.MerchantOrderSummary;
import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 根据ID查询订单
     */
    Order getOrderById(Integer orderId);

    /**
     * 根据订单号查询订单
     */
    Order getOrderByOrderNumber(String orderNumber);

    /**
     * 根据用户ID查询订单
     */
    List<Order> getOrdersByUserId(Integer userId);

    /**
     * 查询所有订单
     */
    List<Order> getAllOrders();

    /**
     * 分页查询订单
     */
    List<Order> getOrdersByPage(int page, int size);

    /**
     * 根据状态分页查询订单
     */
    List<Order> getOrdersByStatusWithPage(OrderStatus status, int page, int size);

    /**
     * 创建订单
     */
    boolean createOrder(Order order);

    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(Integer orderId, OrderStatus status);

    /**
     * 取消订单
     */
    boolean cancelOrder(Integer orderId);

    /**
     * 管理员取消订单
     */
    boolean adminCancelOrder(Integer orderId);

    /**
     * 删除订单（软删除）
     */
    boolean deleteOrder(Integer orderId);

    /**
     * 获取订单总数
     */
    Integer getOrderCount();

    /**
     * 根据状态统计订单数量
     */
    Integer getOrderCountByStatus(OrderStatus status);

    // =============== 商家专用方法 ===============

    /**
     * 根据商家ID获取订单总数
     * 注意：由于订单表没有直接的merchant_id字段，
     * 这个方法需要通过订单项关联商品表来统计
     * 
     * @param merchantId 商家ID
     * @return 订单总数
     */
    Integer getOrderCountByMerchant(String merchantId);

    // =============== 新增方法 ===============

    /**
     * 从购物车创建订单
     */
    Order createOrderFromCart(Integer userId, String shippingAddress, String contactPhone,
            String paymentMethod, String remark);

    /**
     * 根据指定结算商品创建订单
     */
    Order createOrderFromCart(Integer userId, String shippingAddress, String contactPhone,
            String paymentMethod, String remark, List<CartItem> checkoutItems);

    /**
     * 支付订单并扣减库存
     */
    boolean payOrder(Integer orderId, Integer userId);

    /**
     * 根据用户ID分页查询订单
     */
    List<Order> getOrdersByUserIdWithPage(Integer userId, int page, int size);

    /**
     * 根据用户ID和状态分页查询订单
     */
    List<Order> getOrdersByUserIdAndStatus(Integer userId, OrderStatus status, int page, int size);

    /**
     * 获取用户订单统计信息
     */
    Map<String, Object> getUserOrderStats(Integer userId);

    /**
     * 获取订单总数（用于统计）
     */
    long getTotalOrderCount();

    /**
     * 获取今日新增订单数
     */
    long getTodayNewOrderCount();

    List<MerchantOrderSummary> getOrdersByMerchant(String merchantId, String status, int page, int size);

    Integer getOrderCountByMerchant(String merchantId, String status);
}
