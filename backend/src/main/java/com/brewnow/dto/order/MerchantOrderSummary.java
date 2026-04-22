package com.brewnow.dto.order;

import com.brewnow.enums.OrderStatus;
import com.brewnow.enums.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MerchantOrderSummary {

    private Integer orderId;
    private String orderNumber;
    private Integer userId;
    private BigDecimal totalAmount;
    private OrderStatus orderStatus;
    private PaymentMethod paymentMethod;
    private LocalDateTime orderDate;
    private Integer itemCount;
    private String productNames;
    private String shippingAddress;
    private String contactPhone;
    private String remark;
}
