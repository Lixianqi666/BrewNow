package com.brewnow.entity;

import com.brewnow.enums.MerchantStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家实体类
 * 对应数据库merchants表
 */
@Data
public class Merchant {

    /**
     * 商家ID，主键
     */
    private String merchantId;

    /**
     * 关联的用户ID
     */
    private Integer userId;

    /**
     * 企业名称
     */
    private String companyName;

    /**
     * 营业执照号
     */
    private String businessLicense;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 经营地址
     */
    private String businessAddress;

    /**
     * 商家描述
     */
    private String description;

    /**
     * 商家状态
     */
    private MerchantStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 审核时间
     */
    private LocalDateTime approveTime;
}