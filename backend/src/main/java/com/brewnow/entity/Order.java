package com.brewnow.entity;

import com.brewnow.enums.OrderStatus;
import com.brewnow.enums.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 * 对应数据库orders表
 */
@Data
public class Order {

    /**
     * 订单ID，主键
     */
    private Integer orderId;

    /**
     * 用户ID，外键
     */
    private Integer userId;

    /**
     * 订单号，唯一
     */
    private String orderNumber;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 订单状态
     */
    private OrderStatus orderStatus;

    /**
     * 支付方式
     */
    private PaymentMethod paymentMethod;

    /**
     * 订单日期
     */
    private LocalDateTime orderDate;

    /**
     * 配送地址
     */
    private String shippingAddress;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否已扣减库存
     */
    private Integer stockDeducted;

    /**
     * 删除时间（软删除）
     */
    private LocalDateTime deletedAt;
}
