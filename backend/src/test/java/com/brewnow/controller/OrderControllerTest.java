package com.brewnow.controller;

import com.brewnow.common.Result;
import com.brewnow.entity.Order;
import com.brewnow.service.CartService;
import com.brewnow.service.OrderItemService;
import com.brewnow.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderItemService orderItemService;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderController orderController;

    @Test
    void createOrderFromCartShouldReturnCreatedOrder() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("userId")).thenReturn(9);

        Order order = new Order();
        order.setOrderId(1001);
        order.setUserId(9);
        order.setOrderNumber("ORD20260326001");
        order.setTotalAmount(BigDecimal.valueOf(188));

        when(orderService.createOrderFromCart(9, "郑州市金水区", "13800000000", "ALIPAY", "尽快发货", null))
                .thenReturn(order);

        Result<Order> result = orderController.createOrderFromCart(Map.of(
                "shippingAddress", "郑州市金水区",
                "contactPhone", "13800000000",
                "paymentMethod", "ALIPAY",
                "remark", "尽快发货"
        ), request);

        assertEquals(200, result.getCode());
        assertEquals("ORD20260326001", result.getData().getOrderNumber());
    }

    @Test
    void createOrderWithCheckoutItemsShouldReturnCreatedOrder() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("userId")).thenReturn(9);

        Order order = new Order();
        order.setOrderId(1002);
        order.setUserId(9);
        order.setOrderNumber("ORD20260326002");
        order.setTotalAmount(BigDecimal.valueOf(268));

        when(orderService.createOrderFromCart(eq(9), eq("郑州市金水区"), eq("13800000000"), eq("ALIPAY"), eq("立即购买"), anyList()))
                .thenReturn(order);

        Result<Order> result = orderController.createOrderFromCart(Map.of(
                "shippingAddress", "郑州市金水区",
                "contactPhone", "13800000000",
                "paymentMethod", "ALIPAY",
                "remark", "立即购买",
                "items", java.util.List.of(Map.of(
                        "productId", 32,
                        "quantity", 1
                ))
        ), request);

        assertEquals(200, result.getCode());
        assertEquals("ORD20260326002", result.getData().getOrderNumber());
    }
}
