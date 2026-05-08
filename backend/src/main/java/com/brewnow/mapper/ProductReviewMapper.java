package com.brewnow.mapper;

import com.brewnow.entity.ProductReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProductReviewMapper {

    Integer insert(ProductReview review);

    ProductReview selectByOrderItemId(@Param("orderItemId") Integer orderItemId);

    List<ProductReview> selectByProductId(@Param("productId") Integer productId);

    Map<String, Object> selectReviewSummary(@Param("productId") Integer productId);

    Integer countByProductId(@Param("productId") Integer productId);

    List<ProductReview> selectByMerchantId(@Param("merchantId") String merchantId);
}
