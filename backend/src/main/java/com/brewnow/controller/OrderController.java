package com.brewnow.controller;

import com.brewnow.common.Result;
import com.brewnow.entity.CartItem;
import com.brewnow.entity.Order;
import com.brewnow.entity.OrderItem;
import com.brewnow.enums.OrderStatus;
import com.brewnow.service.OrderService;
import com.brewnow.service.OrderItemService;
import com.brewnow.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/order")
@Tag(name = "订单模块", description = "订单创建、支付、取消、确认收货、删除与统计")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private CartService cartService;

    /**
     * 从购物车创建订单
     */
    @PostMapping("/create")
    @Operation(summary = "创建订单", description = "从购物车或立即购买项创建订单")
    public Result<Order> createOrderFromCart(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            // 从JWT中获取用户ID
            Integer userId = (Integer) httpRequest.getAttribute("userId");

            String shippingAddress = (String) request.get("shippingAddress");
            String contactPhone = (String) request.get("contactPhone");
            String paymentMethod = (String) request.get("paymentMethod");
            String remark = (String) request.get("remark");
            List<CartItem> checkoutItems = parseCheckoutItems(request.get("items"));

            if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
                return Result.error("收货地址不能为空");
            }
            if (contactPhone == null || contactPhone.trim().isEmpty()) {
                return Result.error("联系电话不能为空");
            }

            Order order = orderService.createOrderFromCart(userId, shippingAddress, contactPhone, paymentMethod,
                    remark, checkoutItems);
            if (order != null) {
                return Result.success("订单创建成功", order);
            } else {
                return Result.error("订单创建失败");
            }
        } catch (Exception e) {
            return Result.error("订单创建失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户订单列表
     */
    @GetMapping("/list")
    @Operation(summary = "订单列表", description = "查询当前用户订单列表，可按状态筛选")
    public Result<List<Order>> getUserOrders(HttpServletRequest request,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status) {
        try {
            // 从JWT中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");

            List<Order> orders;
            if (status != null && !status.trim().isEmpty()) {
                try {
                    OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                    orders = orderService.getOrdersByUserIdAndStatus(userId, orderStatus, page, size);
                } catch (IllegalArgumentException e) {
                    return Result.error("无效的订单状态");
                }
            } else {
                orders = orderService.getOrdersByUserIdWithPage(userId, page, size);
            }

            return Result.success(orders);
        } catch (Exception e) {
            return Result.error("获取订单列表失败");
        }
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/detail/{orderId}")
    @Operation(summary = "订单详情", description = "查询指定订单及订单项明细")
    public Result<Map<String, Object>> getOrderDetail(@PathVariable Integer orderId, HttpServletRequest request) {
        try {
            // 从JWT中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");

            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return Result.error("订单不存在");
            }

            // 验证订单是否属于当前用户
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权访问此订单");
            }

            // 获取订单项
            List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(orderId);

            Map<String, Object> orderDetail = new HashMap<>();
            orderDetail.put("order", order);
            orderDetail.put("orderItems", orderItems);

            return Result.success(orderDetail);
        } catch (Exception e) {
            return Result.error("获取订单详情失败");
        }
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel/{orderId}")
    @Operation(summary = "取消订单", description = "用户取消待支付订单")
    public Result<Void> cancelOrder(@PathVariable Integer orderId, HttpServletRequest request) {
        try {
            // 从JWT中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");

            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return Result.error("订单不存在");
            }

            // 验证订单是否属于当前用户
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权操作此订单");
            }

            // 只有待支付状态的订单才能取消
            if (order.getOrderStatus() != OrderStatus.PENDING) {
                return Result.error("当前订单状态不允许取消");
            }

            boolean success = orderService.updateOrderStatus(orderId, OrderStatus.CANCELLED);
            if (success) {
                return Result.success("订单已取消", null);
            } else {
                return Result.error("取消订单失败");
            }
        } catch (Exception e) {
            return Result.error("取消订单失败");
        }
    }

    /**
     * 确认收货
     */
    @PutMapping("/confirm/{orderId}")
    @Operation(summary = "确认收货", description = "用户确认已发货订单收货")
    public Result<Void> confirmOrder(@PathVariable Integer orderId, HttpServletRequest request) {
        try {
            // 从JWT中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");

            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return Result.error("订单不存在");
            }

            // 验证订单是否属于当前用户
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权操作此订单");
            }

            // 只有已发货状态的订单才能确认收货
            if (order.getOrderStatus() != OrderStatus.SHIPPED) {
                return Result.error("当前订单状态不允许确认收货");
            }

            boolean success = orderService.updateOrderStatus(orderId, OrderStatus.DELIVERED);
            if (success) {
                return Result.success("订单已确认收货", null);
            } else {
                return Result.error("确认收货失败");
            }
        } catch (Exception e) {
            return Result.error("确认收货失败");
        }
    }

    /**
     * 删除订单（软删除，仅已取消订单可删除）
     */
    @DeleteMapping("/delete/{orderId}")
    @Operation(summary = "删除订单", description = "删除已取消订单，采用软删除")
    public Result<Void> deleteOrder(@PathVariable Integer orderId, HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");

            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return Result.error("订单不存在");
            }

            if (!order.getUserId().equals(userId)) {
                return Result.error("无权操作此订单");
            }

            if (order.getOrderStatus() != OrderStatus.CANCELLED) {
                return Result.error("仅已取消订单支持删除");
            }

            boolean success = orderService.deleteOrder(orderId);
            if (success) {
                return Result.success("订单已删除", null);
            }
            return Result.error("删除订单失败");
        } catch (Exception e) {
            return Result.error("删除订单失败");
        }
    }

    /**
     * 模拟支付
     */
    @PutMapping("/pay/{orderId}")
    @Operation(summary = "模拟支付", description = "对待支付订单执行模拟支付，推动订单进入已支付状态")
    public Result<Void> payOrder(@PathVariable Integer orderId, HttpServletRequest request) {
        try {
            // 从JWT中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");

            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return Result.error("订单不存在");
            }

            // 验证订单是否属于当前用户
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权操作此订单");
            }

            // 只有待支付状态的订单才能支付
            if (order.getOrderStatus() != OrderStatus.PENDING) {
                return Result.error("当前订单状态不允许支付");
            }

            boolean success = orderService.payOrder(orderId, userId);
            if (success) {
                return Result.success("支付成功", null);
            } else {
                return Result.error("支付失败");
            }
        } catch (Exception e) {
            return Result.error("支付失败");
        }
    }

    /**
     * 获取订单统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "订单统计", description = "获取当前用户订单统计和状态分布")
    public Result<Map<String, Object>> getOrderStats(HttpServletRequest request) {
        try {
            // 从JWT中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");

            Map<String, Object> stats = orderService.getUserOrderStats(userId);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取订单统计失败");
        }
    }

    @SuppressWarnings("unchecked")
    private List<CartItem> parseCheckoutItems(Object itemsObject) {
        if (!(itemsObject instanceof List<?> items) || items.isEmpty()) {
            return null;
        }

        List<CartItem> checkoutItems = new ArrayList<>();
        for (Object itemObject : items) {
            if (!(itemObject instanceof Map<?, ?> itemMap)) {
                continue;
            }

            CartItem cartItem = new CartItem();
            cartItem.setCartItemId(toInteger(itemMap.get("cartItemId")));
            cartItem.setProductId(toInteger(itemMap.get("productId")));
            cartItem.setQuantity(toInteger(itemMap.get("quantity")));
            checkoutItems.add(cartItem);
        }
        return checkoutItems;
    }

    private Integer toInteger(Object value) {
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String string && !string.trim().isEmpty()) {
            try {
                return Integer.parseInt(string.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
