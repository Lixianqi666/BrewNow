package com.brewnow.controller;

import com.brewnow.common.Result;
import com.brewnow.entity.Merchant;
import com.brewnow.entity.User;
import com.brewnow.service.MinioStorageService;
import com.brewnow.service.MerchantService;
import com.brewnow.service.UserService;
import com.brewnow.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@Validated
@Tag(name = "用户模块", description = "用户注册登录、资料维护、头像上传与商家注册登录")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MinioStorageService minioStorageService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "普通消费者账号注册")
    public Result<Void> register(@Valid @RequestBody User user) {
            boolean success = userService.register(user);
            if (success) {
                return Result.success("注册成功", null);
            } else {
                return Result.error("注册失败");
            }

    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "普通消费者账号登录")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
            String account = loginData.get("account");
            String password = loginData.get("password");

            String token = userService.login(account, password);
            Integer userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getUserById(userId);

            Map<String, Object> loginResult = new HashMap<>();
            loginResult.put("token", token);
            loginResult.put("userInfo", user);
            return Result.success("登录成功", loginResult);

    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    @Operation(summary = "上传头像", description = "上传用户头像到 MinIO，并更新用户资料")
    public Result<Map<String, String>> uploadAvatar(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam("file") MultipartFile file) {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Result.unauthorized("未登录或登录已过期");
            }

            if (file == null || file.isEmpty()) {
                return Result.error("请上传头像文件");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("仅支持图片文件");
            }

            String token = authHeader.substring(7);
            Integer userId;
            try {
                userId = jwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                return Result.unauthorized("登录状态无效，请重新登录");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = ".jpg";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }

            String filename = "avatar_" + userId + "_" + UUID.randomUUID().toString().replace("-", "") + extension;
            String avatarUrl;
            try {
                avatarUrl = minioStorageService.uploadImage(file, "avatars", filename);
            } catch (RuntimeException e) {
                return Result.error(e.getMessage());
            }
            User user = new User();
            user.setUserId(userId);
            user.setAvatarUrl(avatarUrl);
            boolean success = userService.updateUser(user);
            if (!success) {
                return Result.error("头像更新失败");
            }

            Map<String, String> result = new HashMap<>();
            result.put("avatarUrl", avatarUrl);
            return Result.success("头像上传成功", result);
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{userId}")
    @Operation(summary = "用户详情", description = "根据用户 ID 查询用户信息")
    public Result<User> getUserById(@PathVariable @NotNull Integer userId) {
            User user = userService.getUserById(userId);
            if (user != null) {
                return Result.success(user);
            } else {
                return Result.notFound("用户不存在");
            }

    }

    /**
     * 根据账号查询用户
     */
    @GetMapping("/account/{account}")
    @Operation(summary = "账号查询用户", description = "根据账号查询用户信息")
    public Result<User> getUserByAccount(@PathVariable @NotBlank String account) {
            User user = userService.getUserByAccount(account);
            if (user != null) {
                return Result.success(user);
            } else {
                return Result.notFound("用户不存在");
            }

    }

    /**
     * 查询所有用户（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "用户列表", description = "分页查询用户列表")
    public Result<List<User>> getAllUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
            List<User> users = userService.getAllUsers(page, size);
            return Result.success(users);

    }

    /**
     * 根据条件查询用户
     */
    @PostMapping("/search")
    @Operation(summary = "条件查询用户", description = "根据用户条件对象查询用户")
    public Result<List<User>> getUsersByCondition(@RequestBody User user) {
            List<User> users = userService.getUsersByCondition(user);
            return Result.success(users);

    }

    /**
     * 统计用户总数
     */
    @GetMapping("/count")
    @Operation(summary = "用户总数", description = "获取用户总数")
    public Result<Integer> getUserCount() {
            Integer count = userService.getUserCount();
            return Result.success(count);

    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    @Operation(summary = "更新用户", description = "更新用户基础资料")
    public Result<Void> updateUser(@Valid @RequestBody User user) {
            boolean success = userService.updateUser(user);
            if (success) {
                return Result.success("更新成功", null);
            } else {
                return Result.error("更新失败");
            }

    }

    /**
     * 修改密码
     */
    @PutMapping("/change-password")
    @Operation(summary = "修改密码", description = "修改当前用户密码")
    public Result<Void> changePassword(@RequestBody Map<String, Object> passwordData) {
            Integer userId = (Integer) passwordData.get("userId");
            String oldPassword = (String) passwordData.get("oldPassword");
            String newPassword = (String) passwordData.get("newPassword");

            boolean success = userService.changePassword(userId, oldPassword, newPassword);
            if (success) {
                return Result.success("密码修改成功", null);
            } else {
                return Result.error("密码修改失败");
            }

    }

    /**
     * 删除用户（软删除）
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "删除用户", description = "按用户 ID 删除用户")
    public Result<Void> deleteUser(@PathVariable @NotNull Integer userId) {
            boolean success = userService.deleteUser(userId);
            if (success) {
                return Result.success("删除成功", null);
            } else {
                return Result.error("删除失败");
            }

    }

    /**
     * 检查账号是否存在
     */
    @GetMapping("/check-account/{account}")
    @Operation(summary = "检查账号", description = "检查账号是否已存在")
    public Result<Boolean> checkAccount(@PathVariable @NotBlank String account) {
            boolean exists = userService.isAccountExists(account);
            return Result.success(exists);

    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username/{username}")
    @Operation(summary = "检查用户名", description = "检查用户名是否已存在")
    public Result<Boolean> checkUsername(@PathVariable @NotBlank String username) {
            boolean exists = userService.isUsernameExists(username);
            return Result.success(exists);

    }

    /**
     * 商家注册
     */
    @PostMapping("/register-merchant")
    @Operation(summary = "商家注册", description = "注册商家账号和商家资料")
    public Result<Void> registerMerchant(@RequestBody Map<String, Object> registerData) {
            // 解析用户信息
            User user = new User();
            user.setPhone((String) registerData.get("phone"));
            user.setPassword((String) registerData.get("password"));
            user.setUsername((String) registerData.get("username"));

            // 解析商家信息
            Merchant merchant = new Merchant();
            merchant.setMerchantId((String) registerData.get("merchantId"));
            merchant.setCompanyName((String) registerData.get("companyName"));
            merchant.setContactPerson((String) registerData.get("contactPerson"));
            merchant.setContactPhone((String) registerData.get("contactPhone"));
            merchant.setBusinessAddress((String) registerData.get("businessAddress"));
            merchant.setDescription((String) registerData.get("description"));

            boolean success = userService.registerMerchant(user, merchant);
            if (success) {
                return Result.success("商家注册成功", null);
            } else {
                return Result.error("商家注册失败");
            }

    }

    /**
     * 商家登录
     */
    @PostMapping("/merchant-login")
    @Operation(summary = "商家登录", description = "商家账号登录")
    public Result<Map<String, Object>> merchantLogin(@RequestBody Map<String, String> loginData) {
            String merchantId = loginData.get("merchantId");
            if (merchantId == null || merchantId.trim().isEmpty()) {
                merchantId = loginData.get("account");
            }
            String password = loginData.get("password");

            String token = userService.merchantLogin(merchantId, password);
            Merchant merchant = merchantService.getMerchantById(merchantId);

            Map<String, Object> loginResult = new HashMap<>();
            loginResult.put("token", token);
            loginResult.put("merchantInfo", merchant);
            return Result.success("登录成功", loginResult);

    }

    /**
     * 检查商家ID是否存在
     */
    @GetMapping("/check-merchant-id/{merchantId}")
    @Operation(summary = "检查商家ID", description = "检查商家标识是否已存在")
    public Result<Boolean> checkMerchantId(@PathVariable @NotBlank String merchantId) {
            boolean exists = userService.isMerchantIdExists(merchantId);
            return Result.success(exists);

    }
}
