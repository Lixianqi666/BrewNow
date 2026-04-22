package com.brewnow.mapper;

import com.brewnow.entity.UserBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserBehaviorMapper {

    Integer insert(UserBehavior userBehavior);

    List<UserBehavior> selectAll();

    List<UserBehavior> selectByUserId(@Param("userId") Integer userId);

    Integer countByUserId(@Param("userId") Integer userId);

    Integer countAll();

    Integer countDistinctUsers();

    Integer countDistinctProducts();

    Integer countByType(@Param("behaviorType") String behaviorType);

    List<UserBehavior> selectRecent(@Param("limit") int limit);
}
