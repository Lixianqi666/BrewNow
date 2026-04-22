package com.brewnow.enums;

/**
 * 管理员角色枚举
 */
public enum AdminRole {
    SUPER_ADMIN("超级管理员"),
    ADMIN("管理员"),
    OPERATOR("运营专员");

    private final String description;

    AdminRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}