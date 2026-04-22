package com.brewnow.service;

import com.brewnow.entity.User;
import com.brewnow.entity.Merchant;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     * 
     * @param user 用户信息
     * @return 注册是否成功
     */
    boolean register(User user);

    /**
     * 用户登录
     * 
     * @param account  账号
     * @param password 密码
     * @return JWT Token
     */
    String login(String account, String password);

    /**
     * 根据ID查询用户
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserById(Integer userId);

    /**
     * 根据账号查询用户
     * 
     * @param account 账号
     * @return 用户信息
     */
    User getUserByAccount(String account);

    /**
     * 查询所有用户（分页）
     * 
     * @param page 页码
     * @param size 页大小
     * @return 用户列表
     */
    List<User> getAllUsers(Integer page, Integer size);

    /**
     * 根据条件查询用户
     * 
     * @param user 查询条件
     * @return 用户列表
     */
    List<User> getUsersByCondition(User user);

    /**
     * 统计用户总数
     * 
     * @return 用户总数
     */
    Integer getUserCount();

    /**
     * 更新用户信息
     * 
     * @param user 用户信息
     * @return 更新是否成功
     */
    boolean updateUser(User user);

    /**
     * 修改密码
     * 
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改是否成功
     */
    boolean changePassword(Integer userId, String oldPassword, String newPassword);

    /**
     * 删除用户（软删除）
     * 
     * @param userId 用户ID
     * @return 删除是否成功
     */
    boolean deleteUser(Integer userId);

    /**
     * 检查账号是否存在
     * 
     * @param account 账号
     * @return 是否存在
     */
    boolean isAccountExists(String account);

    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    boolean isUsernameExists(String username);

    /**
     * 商家注册
     * 
     * @param user     用户信息
     * @param merchant 商家信息
     * @return 注册是否成功
     */
    boolean registerMerchant(User user, Merchant merchant);

    /**
     * 商家登录
     *
     * @param merchantId 商家ID
     * @param password   密码
     * @return JWT Token
     */
    String merchantLogin(String merchantId, String password);

    /**
     * 检查商家ID是否存在
     * 
     * @param merchantId 商家ID
     * @return 是否存在
     */
    boolean isMerchantIdExists(String merchantId);

    /**
     * 获取用户总数（用于统计）
     * 
     * @return 用户总数
     */
    long getTotalUserCount();

    /**
     * 获取今日新增用户数
     * 
     * @return 今日新增用户数
     */
    long getTodayNewUserCount();
}
