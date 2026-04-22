package com.brewnow.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品评价实体
 */
@Data
public class ProductReview {

    private Long reviewId;
    private Integer orderId;
    private Integer orderItemId;
    private Integer userId;
    private Integer productId;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private String username;
    private String avatarUrl;
    private String productNameSnapshot;
    private String imageUrlSnapshot;
}
