package com.brewnow.entity;

import com.brewnow.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 * 对应数据库products表
 */
@Data
public class Product {

    /**
     * 商品ID，主键
     */
    private Integer productId;

    /**
     * 商家ID，关联商家
     */
    private String merchantId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 种类/分类
     */
    private String category;

    /**
     * 茶叶标签
     */
    private String teaTags;

    /**
     * 产地
     */
    private String originPlace;

    /**
     * 口感特征
     */
    private String flavorProfile;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 库存数量
     */
    private Integer stockQuantity;

    /**
     * 库存预警阈值
     */
    private Integer warningStock;

    /**
     * 适用设备
     */
    private String compatibleDevices;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品图片URL
     */
    private String imageUrl;

    /**
     * 商品状态
     */
    private ProductStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记（0正常 1已删除）
     */
    private Integer isDeleted;

    /**
     * 平均评分（非数据库持久字段）
     */
    private Double averageRating;

    /**
     * 评价数量（非数据库持久字段）
     */
    private Integer reviewCount;
}
