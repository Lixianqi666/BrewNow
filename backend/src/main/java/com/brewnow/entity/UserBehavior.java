package com.brewnow.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户行为日志实体
 */
@Data
public class UserBehavior {

    private Long id;
    private Integer userId;
    private Integer productId;
    private String behaviorType;
    private BigDecimal behaviorWeight;
    private LocalDateTime createdAt;
}
