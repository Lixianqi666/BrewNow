package com.brewnow.enums;

/**
 * 商家状态枚举
 */
public enum MerchantStatus {
    PENDING("待审核"),
    APPROVED("已审核"),
    REJECTED("被拒绝"),
    SUSPENDED("已暂停");

    private final String description;

    MerchantStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}