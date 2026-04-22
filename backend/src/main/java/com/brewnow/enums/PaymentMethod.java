package com.brewnow.enums;

/**
 * 支付方式枚举
 */
public enum PaymentMethod {
    ALIPAY("支付宝"),
    WECHAT("微信支付"),
    CASH("货到付款"),
    CREDIT_CARD("信用卡");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}