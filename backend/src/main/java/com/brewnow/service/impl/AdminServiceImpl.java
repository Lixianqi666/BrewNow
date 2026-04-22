package com.brewnow.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.brewnow.entity.Admin;
import com.brewnow.mapper.AdminMapper;
import com.brewnow.service.AdminService;
import com.brewnow.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员服务实现类
 */
@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String login(String username, String password) {
        // 参数校验
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            throw new RuntimeException("用户名或密码不能为空");
        }

        // 查询管理员
        Admin admin = adminMapper.selectByUsername(username);
        if (admin == null) {
            System.out.println("管理员登录失败: 账号不存在 - " + username);
            throw new RuntimeException("管理员账号不存在");
        }

        System.out.println("管理员登录验证 - 用户名: " + username + ", 管理员ID: " + admin.getAdminId());

        // 检查账号状态
        if (admin.getStatus() != com.brewnow.enums.Status.ACTIVE) {
            System.out.println("管理员登录失败: 账号被禁用 - " + username);
            throw new RuntimeException("账号已被禁用，请联系系统管理员");
        }

        // 验证密码 - 支持明文和BCrypt两种格式
        boolean passwordValid = false;
        if (admin.getPassword().startsWith("$2a$")) {
            // BCrypt加密密码验证
            passwordValid = BCrypt.checkpw(password, admin.getPassword());
            System.out.println("管理员BCrypt密码验证结果: " + passwordValid);
        } else {
            // 明文密码验证
            passwordValid = password.equals(admin.getPassword());
            System.out.println("管理员明文密码验证结果: " + passwordValid);
        }

        if (!passwordValid) {
            System.out.println("管理员登录失败: 密码错误 - " + username);
            throw new RuntimeException("密码错误");
        }

        System.out.println("管理员登录成功 - 用户名: " + username + ", 角色: " + admin.getRole());

        // 更新最后登录时间
        try {
            adminMapper.updateLastLoginTime(admin.getAdminId());
            System.out.println("管理员最后登录时间更新成功");
        } catch (Exception e) {
            System.out.println("管理员最后登录时间更新失败: " + e.getMessage());
            // 不影响登录流程，仅记录日志
        }

        // 生成包含角色信息的Token
        String token = jwtUtil.generateToken(admin.getAdminId(), "admin", admin.getRole().name(), null);

        System.out.println("管理员JWT Token生成成功 - 管理员ID: " + admin.getAdminId() + ", 角色: " + admin.getRole().name());

        return token;
    }

    @Override
    @Transactional(readOnly = true)
    public Admin getAdminById(Integer adminId) {
        if (adminId == null) {
            throw new RuntimeException("管理员ID不能为空");
        }

        Admin admin = adminMapper.selectById(adminId);
        if (admin != null) {
            // 清除密码字段
            admin.setPassword(null);
        }
        return admin;
    }

    @Override
    @Transactional(readOnly = true)
    public Admin getCurrentAdmin(Integer adminId) {
        return getAdminById(adminId);
    }

    @Override
    @Transactional(readOnly = true)
    public Admin getAdminByUsername(String username) {
        if (StrUtil.isBlank(username)) {
            throw new RuntimeException("用户名不能为空");
        }

        Admin admin = adminMapper.selectByUsername(username);
        if (admin != null) {
            // 清除密码字段
            admin.setPassword(null);
        }
        return admin;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Admin> getAllAdmins() {
        List<Admin> admins = adminMapper.selectAll();

        // 清除密码字段
        admins.forEach(admin -> admin.setPassword(null));

        return admins;
    }

    @Override
    public boolean updateAdmin(Admin admin) {
        if (admin == null || admin.getAdminId() == null) {
            throw new RuntimeException("管理员信息不完整");
        }

        return adminMapper.updateById(admin) > 0;
    }

    @Override
    public boolean changePassword(Integer adminId, String oldPassword, String newPassword) {
        // 参数校验
        if (adminId == null || StrUtil.isBlank(oldPassword) || StrUtil.isBlank(newPassword)) {
            throw new RuntimeException("参数不完整");
        }

        // 查询管理员
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }

        // 验证旧密码
        if (!oldPassword.equals(admin.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 更新密码（这里应该加密，但为了兼容现有数据，暂时使用明文）
        return adminMapper.updatePassword(adminId, newPassword) > 0;
    }
}
