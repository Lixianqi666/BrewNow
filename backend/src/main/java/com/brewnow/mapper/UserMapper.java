package com.brewnow.mapper;

import com.brewnow.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper {

    /**
     * 根据ID查询用户
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    User selectById(Integer userId);

    /**
     * 根据账号查询用户
     * 
     * @param account 账号
     * @return 用户信息
     */
    User selectByAccount(String account);

    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    User selectByUsername(String username);

    /**
     * 查询所有用户（分页）
     * 
     * @param offset 偏移量
     * @param limit  限制数量
     * @return 用户列表
     */
    List<User> selectAll(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 根据条件查询用户
     * 
     * @param user 查询条件
     * @return 用户列表
     */
    List<User> selectByCondition(User user);

    /**
     * 统计用户总数
     * 
     * @return 用户总数
     */
    Integer countAll();

    /**
     * 根据条件统计用户数量
     * 
     * @param user 查询条件
     * @return 用户数量
     */
    Integer countByCondition(User user);

    /**
     * 插入用户
     * 
     * @param user 用户信息
     * @return 插入成功的记录数
     */
    Integer insert(User user);

    /**
     * 根据ID更新用户
     * 
     * @param user 用户信息
     * @return 更新成功的记录数
     */
    Integer updateById(User user);

    /**
     * 根据ID删除用户（物理删除）
     * 
     * @param userId 用户ID
     * @return 删除成功的记录数
     */
    Integer deleteById(Integer userId);

    /**
     * 根据ID软删除用户
     * 
     * @param userId 用户ID
     * @return 软删除成功的记录数
     */
    Integer softDeleteById(Integer userId);

    /**
     * 检查账号是否存在
     * 
     * @param account 账号
     * @return 是否存在
     */
    boolean existsByAccount(String account);

    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 根据手机号查询用户
     * 
     * @param phone 手机号
     * @return 用户信息
     */
    User selectByPhone(String phone);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    User selectByEmail(String email);

    /**
     * 根据商家ID查询商家用户
     *
     * @param merchantId 商家ID
     * @return 用户信息
     */
    User selectMerchantByMerchantId(@Param("merchantId") String merchantId);

    /**
     * 统计今日新增用户数
     * 
     * @return 今日新增用户数
     */
    Integer countTodayNewUsers();
}
