package com.brewnow.entity;

import com.brewnow.enums.Gender;
import com.brewnow.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库users表
 */
@Data
public class User {

    /**
     * 用户ID，主键
     */
    private Integer userId;

    /**
     * 账号，唯一
     */
    private String account;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 性别
     */
    private Gender gender;

    /**
     * 注册时间
     */
    private LocalDateTime registerTime;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 地址
     */
    private String address;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 删除时间（软删除）
     */
    private LocalDateTime deletedAt;

    /**
     * 用户角色
     */
    private UserRole role;

    /**
     * 商家ID（仅商家用户有值）
     */
    private String merchantId;
}
