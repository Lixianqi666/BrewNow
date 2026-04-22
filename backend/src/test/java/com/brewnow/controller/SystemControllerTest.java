package com.brewnow.controller;

import com.brewnow.common.Result;
import com.brewnow.service.OrderService;
import com.brewnow.service.ProductService;
import com.brewnow.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private SystemController systemController;

    @Test
    void docsShouldReturnSwaggerEndpoints() {
        Result<Map<String, String>> result = systemController.docs();

        assertEquals(200, result.getCode());
        assertEquals("/api/swagger-ui/index.html", result.getData().get("swaggerUi"));
        assertEquals("/api/v3/api-docs", result.getData().get("openApiJson"));
    }

    @Test
    void getSystemStatsShouldReturnAggregatedCounts() {
        when(productService.getTotalProductCount()).thenReturn(32L);
        when(userService.getTotalUserCount()).thenReturn(18L);
        when(orderService.getTotalOrderCount()).thenReturn(65L);
        when(userService.getTodayNewUserCount()).thenReturn(3L);
        when(orderService.getTodayNewOrderCount()).thenReturn(5L);

        Result<Map<String, Object>> result = systemController.getSystemStats();

        assertEquals(200, result.getCode());
        assertEquals(32L, result.getData().get("totalProducts"));
        assertEquals(18L, result.getData().get("totalUsers"));
        assertEquals(65L, result.getData().get("totalOrders"));
        assertTrue(result.getData().containsKey("todayNewUsers"));
        assertTrue(result.getData().containsKey("todayNewOrders"));
    }
}
