package com.brewnow.service;

import com.brewnow.entity.Admin;

import java.util.List;

/**
 * 管理员服务接口
 */
public interface AdminService {

    /**
     * 管理员登录
     * 
     * @param username 用户名
     * @param password 密码
     * @return JWT Token
     */
    String login(String username, String password);

    /**
     * 根据ID查询管理员
     * 
     * @param adminId 管理员ID
     * @return 管理员信息
     */
    Admin getAdminById(Integer adminId);

    /**
     * 获取当前管理员信息
     *
     * @param adminId 管理员ID
     * @return 当前管理员信息
     */
    Admin getCurrentAdmin(Integer adminId);

    /**
     * 根据用户名查询管理员
     * 
     * @param username 用户名
     * @return 管理员信息
     */
    Admin getAdminByUsername(String username);

    /**
     * 查询所有管理员
     * 
     * @return 管理员列表
     */
    List<Admin> getAllAdmins();

    /**
     * 更新管理员信息
     * 
     * @param admin 管理员信息
     * @return 更新是否成功
     */
    boolean updateAdmin(Admin admin);

    /**
     * 修改管理员密码
     * 
     * @param adminId     管理员ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改是否成功
     */
    boolean changePassword(Integer adminId, String oldPassword, String newPassword);
}
