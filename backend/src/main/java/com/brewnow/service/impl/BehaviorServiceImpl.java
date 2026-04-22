package com.brewnow.service.impl;

import com.brewnow.entity.UserBehavior;
import com.brewnow.mapper.UserBehaviorMapper;
import com.brewnow.service.BehaviorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class BehaviorServiceImpl implements BehaviorService {

    private static final Map<String, BigDecimal> WEIGHT_MAP = Map.of(
            "VIEW", BigDecimal.ONE,
            "FAVORITE", BigDecimal.valueOf(2),
            "CART", BigDecimal.valueOf(3),
            "PURCHASE", BigDecimal.valueOf(4)
    );

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    @Override
    public void recordBehavior(Integer userId, Integer productId, String behaviorType) {
        if (userId == null || productId == null || behaviorType == null || behaviorType.trim().isEmpty()) {
            return;
        }

        String normalizedType = behaviorType.trim().toUpperCase();
        BigDecimal weight = WEIGHT_MAP.get(normalizedType);
        if (weight == null) {
            return;
        }

        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setProductId(productId);
        behavior.setBehaviorType(normalizedType);
        behavior.setBehaviorWeight(weight);
        behavior.setCreatedAt(LocalDateTime.now());
        userBehaviorMapper.insert(behavior);
    }

    @Override
    public List<UserBehavior> getAllBehaviors() {
        return userBehaviorMapper.selectAll();
    }

    @Override
    public Integer getBehaviorCountByUser(Integer userId) {
        return userBehaviorMapper.countByUserId(userId);
    }
}
