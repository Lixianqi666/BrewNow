package com.brewnow.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车实体类
 * 对应数据库shopping_carts表
 */
@Data
public class ShoppingCart {

    /**
     * 购物车ID，主键
     */
    private Integer cartId;

    /**
     * 用户ID，唯一（一对一关系）
     */
    private Integer userId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}