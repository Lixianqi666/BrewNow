package com.brewnow.controller;

import com.brewnow.common.Result;
import com.brewnow.service.BehaviorService;
import com.brewnow.service.CartService;
import com.brewnow.service.RecommendationCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private BehaviorService behaviorService;

    @Mock
    private RecommendationCacheService recommendationCacheService;

    @InjectMocks
    private CartController cartController;

    @Test
    void addToCartShouldRecordBehaviorAndEvictRecommendationCache() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("userId")).thenReturn(12);
        when(cartService.addToCart(12, 5, 2)).thenReturn(true);

        Result<Void> result = cartController.addToCart(Map.of(
                "productId", 5,
                "quantity", 2
        ), request);

        assertEquals(200, result.getCode());
        verify(behaviorService).recordBehavior(12, 5, "CART");
        verify(recommendationCacheService).evictRecommendationCaches(12, 5);
    }
}
