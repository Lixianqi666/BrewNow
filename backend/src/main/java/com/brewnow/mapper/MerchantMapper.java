package com.brewnow.mapper;

import com.brewnow.entity.Merchant;
import com.brewnow.enums.MerchantStatus;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 商家Mapper接口
 */
@Mapper
public interface MerchantMapper {

        /**
         * 根据ID查询商家
         */
        @ResultMap("merchantResultMap")
        Merchant selectById(@Param("merchantId") String merchantId);

        /**
         * 根据用户ID查询商家
         */
        @ResultMap("merchantResultMap")
        Merchant selectByUserId(@Param("userId") Integer userId);

        /**
         * 查询所有商家
         */
        @ResultMap("merchantResultMap")
        List<Merchant> selectAll();

        /**
         * 分页查询商家
         */
        @ResultMap("merchantResultMap")
        List<Merchant> selectByPage(@Param("offset") int offset, @Param("limit") int limit);

        /**
         * 根据状态查询商家
         */
        @ResultMap("merchantResultMap")
        List<Merchant> selectByStatus(@Param("status") MerchantStatus status);

        /**
         * 插入商家
         */
        int insert(Merchant merchant);

        /**
         * 更新商家
         */
        int updateById(Merchant merchant);

        /**
         * 删除商家
         */
        int deleteById(@Param("merchantId") String merchantId);

        /**
         * 统计商家总数
         */
        Integer countAll();

        /**
         * 根据状态统计商家数量
         */
        Integer countByStatus(@Param("status") MerchantStatus status);

        /**
         * 检查商家ID是否存在
         */
        boolean existsByMerchantId(@Param("merchantId") String merchantId);
}