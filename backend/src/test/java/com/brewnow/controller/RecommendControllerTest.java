package com.brewnow.controller;

import com.brewnow.common.Result;
import com.brewnow.entity.Product;
import com.brewnow.service.RecommendationService;
import com.brewnow.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendControllerTest {

    @Mock
    private RecommendationService recommendationService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private RecommendController recommendController;

    @Test
    void getHomeRecommendationsShouldReturnRecommendationList() {
        Product product = new Product();
        product.setProductId(1);
        product.setProductName("明前西湖龙井 100g");
        product.setPrice(BigDecimal.valueOf(128));

        when(jwtUtil.getUserIdFromToken("mock-token")).thenReturn(3);
        when(recommendationService.getHomeRecommendations(3, 4)).thenReturn(List.of(product));

        Result<List<Product>> result = recommendController.getHomeRecommendations("Bearer mock-token", 4);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        assertEquals("明前西湖龙井 100g", result.getData().get(0).getProductName());
    }
}
