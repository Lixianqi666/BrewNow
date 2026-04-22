package com.brewnow.service.impl;

import cn.hutool.core.util.StrUtil;
import com.brewnow.entity.Merchant;
import com.brewnow.entity.User;
import com.brewnow.enums.UserRole;
import com.brewnow.enums.MerchantStatus;
import com.brewnow.mapper.UserMapper;
import com.brewnow.mapper.MerchantMapper;
import com.brewnow.service.UserService;
import com.brewnow.utils.JwtUtil;
import com.brewnow.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务实现类
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean register(User user) {
        // 参数校验
        if (user == null || StrUtil.isBlank(user.getAccount()) || StrUtil.isBlank(user.getPassword())) {
            throw new RuntimeException("用户信息不完整");
        }

        // 检查账号是否已存在
        if (userMapper.existsByAccount(user.getAccount())) {
            throw new RuntimeException("账号已存在");
        }

        // 检查用户名是否已存在
        if (StrUtil.isNotBlank(user.getUsername()) && userMapper.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 设置用户角色（普通消费者）
        if (user.getRole() == null) {
            user.setRole(UserRole.CONSUMER);
        }

        // 新注册用户统一加密存储密码
        user.setPassword(PasswordUtil.encode(user.getPassword()));

        // 插入用户
        return userMapper.insert(user) > 0;
    }

    @Override
    public String login(String account, String password) {
        // 参数校验
        if (StrUtil.isBlank(account) || StrUtil.isBlank(password)) {
            throw new RuntimeException("账号或密码不能为空");
        }

        User user = findUserByLoginIdentifier(account);
        if (user == null) {
            System.out.println("登录失败: 账号不存在 - " + account);
            throw new RuntimeException("账号不存在");
        }

        System.out.println("用户登录验证 - 账号: " + account + ", 用户ID: " + user.getUserId());

        boolean passwordValid = PasswordUtil.matches(password, user.getPassword());
        System.out.println("密码验证结果: " + passwordValid);

        if (!passwordValid) {
            System.out.println("登录失败: 密码错误 - 账号: " + account);
            throw new RuntimeException("密码错误");
        }

        // 老数据迁移：明文密码登录成功后自动升级为BCrypt
        if (!PasswordUtil.isBcryptHash(user.getPassword())) {
            user.setPassword(PasswordUtil.encode(password));
            userMapper.updateById(user);
        }

        System.out.println("登录成功 - 账号: " + account + ", 角色: " + user.getRole());

        // 生成包含角色信息的Token
        String roleString = user.getRole() != null ? user.getRole().name() : UserRole.CONSUMER.name();
        String token = jwtUtil.generateToken(user.getUserId(), "user", roleString, user.getMerchantId());

        System.out.println("JWT Token生成成功 - 用户ID: " + user.getUserId() + ", 角色: " + roleString);

        return token;
    }

    private User findUserByLoginIdentifier(String identifier) {
        if (StrUtil.isBlank(identifier)) {
            return null;
        }

        User user = userMapper.selectByAccount(identifier);
        if (user != null) {
            return user;
        }

        user = userMapper.selectByUsername(identifier);
        if (user != null) {
            return user;
        }

        user = userMapper.selectByPhone(identifier);
        if (user != null) {
            return user;
        }

        return userMapper.selectByEmail(identifier);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Integer userId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        User user = userMapper.selectById(userId);
        if (user != null) {
            // 清除密码字段
            user.setPassword(null);
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByAccount(String account) {
        if (StrUtil.isBlank(account)) {
            throw new RuntimeException("账号不能为空");
        }

        User user = userMapper.selectByAccount(account);
        if (user != null) {
            // 清除密码字段
            user.setPassword(null);
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers(Integer page, Integer size) {
        // 参数校验
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        Integer offset = (page - 1) * size;
        List<User> users = userMapper.selectAll(offset, size);

        // 清除密码字段
        users.forEach(user -> user.setPassword(null));

        return users;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersByCondition(User user) {
        List<User> users = userMapper.selectByCondition(user);

        // 清除密码字段
        users.forEach(u -> u.setPassword(null));

        return users;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getUserCount() {
        return userMapper.countAll();
    }

    @Override
    public boolean updateUser(User user) {
        if (user == null || user.getUserId() == null) {
            throw new RuntimeException("用户信息不完整");
        }

        // 如果修改了用户名，需要检查是否重复
        if (StrUtil.isNotBlank(user.getUsername())) {
            User existingUser = userMapper.selectById(user.getUserId());
            if (existingUser != null && !user.getUsername().equals(existingUser.getUsername())) {
                if (userMapper.existsByUsername(user.getUsername())) {
                    throw new RuntimeException("用户名已存在");
                }
            }
        }

        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        // 参数校验
        if (userId == null || StrUtil.isBlank(oldPassword) || StrUtil.isBlank(newPassword)) {
            throw new RuntimeException("参数不完整");
        }

        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!PasswordUtil.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        user.setPassword(PasswordUtil.encode(newPassword));
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean deleteUser(Integer userId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        return userMapper.softDeleteById(userId) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAccountExists(String account) {
        if (StrUtil.isBlank(account)) {
            return false;
        }

        return userMapper.existsByAccount(account);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUsernameExists(String username) {
        if (StrUtil.isBlank(username)) {
            return false;
        }

        return userMapper.existsByUsername(username);
    }

    @Override
    @Transactional
    public boolean registerMerchant(User user, Merchant merchant) {
        // 参数校验
        if (user == null || merchant == null) {
            throw new RuntimeException("用户信息或商家信息不能为空");
        }

        if (StrUtil.isBlank(user.getPhone()) || StrUtil.isBlank(user.getPassword()) ||
                StrUtil.isBlank(merchant.getMerchantId()) || StrUtil.isBlank(merchant.getCompanyName())) {
            throw new RuntimeException("必填信息不完整");
        }

        // 检查手机号是否已存在
        if (userMapper.selectByPhone(user.getPhone()) != null) {
            throw new RuntimeException("手机号已被注册");
        }

        // 检查商家ID是否已存在
        if (merchantMapper.existsByMerchantId(merchant.getMerchantId())) {
            throw new RuntimeException("商家ID已存在");
        }

        try {
            // 设置用户角色和商家ID
            user.setRole(UserRole.MERCHANT);
            user.setMerchantId(merchant.getMerchantId());
            user.setAccount(merchant.getMerchantId()); // 商家统一使用商家ID作为登录账号
            user.setPassword(PasswordUtil.encode(user.getPassword()));

            // 插入用户记录
            if (userMapper.insert(user) <= 0) {
                throw new RuntimeException("创建用户失败");
            }

            // 设置商家关联的用户ID和初始状态
            merchant.setUserId(user.getUserId());
            merchant.setStatus(MerchantStatus.PENDING);

            // 插入商家记录
            if (merchantMapper.insert(merchant) <= 0) {
                throw new RuntimeException("创建商家信息失败");
            }

            return true;
        } catch (Exception e) {
            throw new RuntimeException("商家注册失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public String merchantLogin(String merchantId, String password) {
        // 参数校验
        if (StrUtil.isBlank(merchantId) || StrUtil.isBlank(password)) {
            throw new RuntimeException("商家ID或密码不能为空");
        }

        // 查询商家用户
        User user = userMapper.selectMerchantByMerchantId(merchantId);
        if (user == null) {
            System.out.println("商家登录失败: 账号不存在 - 商家ID: " + merchantId);
            throw new RuntimeException("商家账号不存在");
        }

        System.out.println("商家登录验证 - 商家ID: " + merchantId + ", 用户ID: " + user.getUserId());

        boolean passwordValid = PasswordUtil.matches(password, user.getPassword());
        System.out.println("密码验证结果: " + passwordValid);

        if (!passwordValid) {
            System.out.println("商家登录失败: 密码错误 - 商家ID: " + merchantId);
            throw new RuntimeException("密码错误");
        }

        // 老数据迁移：明文密码升级
        if (!PasswordUtil.isBcryptHash(user.getPassword())) {
            user.setPassword(PasswordUtil.encode(password));
            userMapper.updateById(user);
        }

        // 检查商家状态
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            System.out.println("商家登录失败: 商家信息不存在 - 商家ID: " + merchantId);
            throw new RuntimeException("商家信息不存在");
        }

        System.out.println("商家状态检查 - 商家ID: " + merchantId + ", 状态: " + merchant.getStatus());

        if (merchant.getStatus() == MerchantStatus.PENDING) {
            System.out.println("商家登录失败: 账号待审核 - 商家ID: " + merchantId);
            throw new RuntimeException("商家账号待审核，请等待管理员审核");
        }

        if (merchant.getStatus() == MerchantStatus.REJECTED) {
            System.out.println("商家登录失败: 账号被拒绝 - 商家ID: " + merchantId);
            throw new RuntimeException("商家账号已被拒绝，请联系管理员");
        }

        if (merchant.getStatus() == MerchantStatus.SUSPENDED) {
            System.out.println("商家登录失败: 账号被暂停 - 商家ID: " + merchantId);
            throw new RuntimeException("商家账号已被暂停，请联系管理员");
        }

        System.out.println("商家登录成功 - 商家ID: " + merchantId + ", 角色: " + user.getRole());

        // 生成包含角色信息的Token
        String token = jwtUtil.generateToken(user.getUserId(), "user", user.getRole().name(), merchantId);

        System.out.println("商家JWT Token生成成功 - 用户ID: " + user.getUserId() + ", 商家ID: " + merchantId);

        return token;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMerchantIdExists(String merchantId) {
        if (StrUtil.isBlank(merchantId)) {
            return false;
        }

        return merchantMapper.existsByMerchantId(merchantId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalUserCount() {
        Integer count = userMapper.countAll();
        return count != null ? count.longValue() : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public long getTodayNewUserCount() {
        Integer count = userMapper.countTodayNewUsers();
        return count != null ? count.longValue() : 0L;
    }
}
