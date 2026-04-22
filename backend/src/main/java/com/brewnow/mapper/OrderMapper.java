package com.brewnow.mapper;

import com.brewnow.entity.Order;
import com.brewnow.enums.OrderStatus;
import com.brewnow.dto.order.MerchantOrderSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单Mapper接口
 */
@Mapper
public interface OrderMapper {

        /**
         * 根据ID查询订单
         * 
         * @param orderId 订单ID
         * @return 订单信息
         */
        Order selectById(@Param("orderId") Integer orderId);

        /**
         * 根据订单号查询订单
         * 
         * @param orderNumber 订单号
         * @return 订单信息
         */
        Order selectByOrderNumber(@Param("orderNumber") String orderNumber);

        /**
         * 根据用户ID查询订单
         * 
         * @param userId 用户ID
         * @return 订单列表
         */
        List<Order> selectByUserId(@Param("userId") Integer userId);

        /**
         * 查询所有订单
         * 
         * @return 订单列表
         */
        List<Order> selectAll();

        /**
         * 分页查询订单
         * 
         * @param offset 偏移量
         * @param limit  限制数量
         * @return 订单列表
         */
        List<Order> selectByPage(@Param("offset") int offset, @Param("limit") int limit);

        /**
         * 根据状态查询订单
         * 
         * @param status 订单状态
         * @return 订单列表
         */
        List<Order> selectByStatus(@Param("status") OrderStatus status);

        /**
         * 根据状态分页查询订单
         *
         * @param status 订单状态
         * @param offset 偏移量
         * @param limit  限制数量
         * @return 订单列表
         */
        List<Order> selectByStatusWithPage(@Param("status") OrderStatus status,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

        /**
         * 根据用户ID和状态查询订单
         * 
         * @param userId 用户ID
         * @param status 订单状态
         * @return 订单列表
         */
        List<Order> selectByUserIdAndStatus(@Param("userId") Integer userId,
                        @Param("status") OrderStatus status);

        /**
         * 查询所有订单（分页）
         * 
         * @param offset 偏移量
         * @param limit  限制数量
         * @return 订单列表
         */
        List<Order> selectAll(@Param("offset") Integer offset, @Param("limit") Integer limit);

        /**
         * 根据条件查询订单
         * 
         * @param order 查询条件
         * @return 订单列表
         */
        List<Order> selectByCondition(Order order);

        /**
         * 根据时间范围查询订单
         * 
         * @param startTime 开始时间
         * @param endTime   结束时间
         * @return 订单列表
         */
        List<Order> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        /**
         * 统计订单总数
         * 
         * @return 订单总数
         */
        Integer countAll();

        /**
         * 根据用户ID统计订单数量
         * 
         * @param userId 用户ID
         * @return 订单数量
         */
        Integer countByUserId(Integer userId);

        /**
         * 根据状态统计订单数量
         * 
         * @param status 订单状态
         * @return 订单数量
         */
        Integer countByStatus(@Param("status") OrderStatus status);

        /**
         * 根据商家统计订单数量
         */
        Integer countByMerchant(@Param("merchantId") String merchantId);

        /**
         * 根据条件统计订单数量
         * 
         * @param order 查询条件
         * @return 订单数量
         */
        Integer countByCondition(Order order);

        /**
         * 插入订单
         * 
         * @param order 订单信息
         * @return 插入成功的记录数
         */
        Integer insert(Order order);

        /**
         * 根据ID更新订单
         * 
         * @param order 订单信息
         * @return 更新成功的记录数
         */
        Integer updateById(Order order);

        /**
         * 更新订单状态
         * 
         * @param orderId 订单ID
         * @param status  新状态
         * @return 更新成功的记录数
         */
        Integer updateStatus(@Param("orderId") Integer orderId, @Param("status") OrderStatus status);

        /**
         * 根据ID删除订单（物理删除）
         * 
         * @param orderId 订单ID
         * @return 删除成功的记录数
         */
        Integer deleteById(Integer orderId);

        /**
         * 根据ID软删除订单
         * 
         * @param orderId 订单ID
         * @return 软删除成功的记录数
         */
        Integer softDeleteById(Integer orderId);

        /**
         * 检查订单号是否存在
         * 
         * @param orderNumber 订单号
         * @return 是否存在
         */
        boolean existsByOrderNumber(String orderNumber);

        /**
         * 生成订单号
         * 
         * @return 订单号
         */
        String generateOrderNumber();

        // =============== 新增方法 ===============

        /**
         * 根据用户ID分页查询订单
         */
        List<Order> selectByUserIdWithPage(@Param("userId") Integer userId,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

        /**
         * 根据用户ID和状态分页查询订单
         */
        List<Order> selectByUserIdAndStatus(@Param("userId") Integer userId,
                        @Param("status") OrderStatus status,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

        /**
         * 根据用户ID和状态统计订单数量
         */
        Integer countByUserIdAndStatus(@Param("userId") Integer userId,
                        @Param("status") OrderStatus status);

        /**
         * 根据用户ID计算总消费金额
         */
        BigDecimal getTotalAmountByUserId(@Param("userId") Integer userId);

        /**
         * 统计今日新增订单数
         */
        Integer countTodayNewOrders();

        List<MerchantOrderSummary> selectByMerchant(@Param("merchantId") String merchantId,
                                                    @Param("status") String status,
                                                    @Param("offset") int offset,
                                                    @Param("limit") int limit);

        Integer countByMerchantWithStatus(@Param("merchantId") String merchantId,
                                          @Param("status") String status);

        Integer countMerchantOrderOwnership(@Param("merchantId") String merchantId,
                                            @Param("orderId") Integer orderId);
}
