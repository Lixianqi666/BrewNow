package com.brewnow.mapper;

import com.brewnow.entity.Product;
import com.brewnow.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserFavoriteMapper {

    UserFavorite selectByUserAndProduct(@Param("userId") Integer userId, @Param("productId") Integer productId);

    Integer insert(UserFavorite favorite);

    Integer restore(@Param("userId") Integer userId, @Param("productId") Integer productId);

    Integer softDelete(@Param("userId") Integer userId, @Param("productId") Integer productId);

    List<Product> selectActiveProductsByUser(@Param("userId") Integer userId);

    Integer countActiveByUserAndProduct(@Param("userId") Integer userId, @Param("productId") Integer productId);

    Integer countAllActive();
}
