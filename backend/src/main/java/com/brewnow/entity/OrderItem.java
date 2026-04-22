package com.brewnow.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单项实体类
 * 对应数据库order_items表
 */
@Data
public class OrderItem {

    /**
     * 订单项ID，主键
     */
    private Integer orderItemId;

    /**
     * 订单ID，外键
     */
    private Integer orderId;

    /**
     * 商品ID，外键
     */
    private Integer productId;

    /**
     * 产品数量
     */
    private Integer quantity;

    /**
     * 产品单价
     */
    private BigDecimal unitPrice;

    /**
     * 小计（由触发器自动计算）
     */
    private BigDecimal subtotal;

    /**
     * 商品名称快照
     */
    private String productNameSnapshot;

    /**
     * 品牌快照
     */
    private String brandSnapshot;

    /**
     * 分类快照
     */
    private String categorySnapshot;

    /**
     * 图片快照
     */
    private String imageUrlSnapshot;

    /**
     * 是否已评价（非数据库持久字段）
     */
    private Boolean reviewed;
}
