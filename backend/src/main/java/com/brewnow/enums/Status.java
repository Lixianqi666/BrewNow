package com.brewnow.enums;

/**
 * 通用状态枚举
 */
public enum Status {
    ACTIVE("激活"),
    INACTIVE("禁用");

    private final String description;

    Status(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}