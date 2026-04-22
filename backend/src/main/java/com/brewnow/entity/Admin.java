package com.brewnow.entity;

import com.brewnow.enums.AdminRole;
import com.brewnow.enums.Status;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员实体类
 * 对应数据库admins表
 */
@Data
public class Admin {

    /**
     * 管理员ID，主键
     */
    private Integer adminId;

    /**
     * 用户名，唯一
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 角色
     */
    private AdminRole role;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号码
     */
    private String mobilePhone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态
     */
    private Status status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
}