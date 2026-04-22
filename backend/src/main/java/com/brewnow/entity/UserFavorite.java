package com.brewnow.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户收藏实体
 */
@Data
public class UserFavorite {

    private Long id;
    private Integer userId;
    private Integer productId;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
}
