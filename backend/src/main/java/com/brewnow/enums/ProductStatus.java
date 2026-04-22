package com.brewnow.enums;

/**
 * 商品状态枚举
 */
public enum ProductStatus {
    ACTIVE("上架"),
    INACTIVE("下架"),
    DISCONTINUED("停产");

    private final String description;

    ProductStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}