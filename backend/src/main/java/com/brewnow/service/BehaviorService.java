package com.brewnow.service;

import com.brewnow.entity.UserBehavior;

import java.util.List;

public interface BehaviorService {

    void recordBehavior(Integer userId, Integer productId, String behaviorType);

    List<UserBehavior> getAllBehaviors();

    Integer getBehaviorCountByUser(Integer userId);
}
