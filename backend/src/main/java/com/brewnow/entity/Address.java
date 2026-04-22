package com.brewnow.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收货地址实体
 */
@Data
public class Address {
    private Integer addressId;
    private Integer userId;
    private String receiverName;
    private String contactPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String tag;
    private Boolean isDefault;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime deletedAt;
}

