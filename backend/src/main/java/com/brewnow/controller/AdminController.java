package com.brewnow.controller;

import com.brewnow.common.Result;
import com.brewnow.entity.Admin;
import com.brewnow.entity.User;
import com.brewnow.entity.Merchant;
import com.brewnow.entity.Product;
import com.brewnow.entity.Order;
import com.brewnow.entity.OrderItem;
import com.brewnow.enums.MerchantStatus;
import com.brewnow.enums.OrderStatus;
import com.brewnow.enums.ProductStatus;
import com.brewnow.service.AdminService;
import com.brewnow.service.ProductImageBackfillService;
import com.brewnow.service.UserService;
import com.brewnow.service.ProductService;
import com.brewnow.service.OrderService;
import com.brewnow.service.OrderItemService;
import com.brewnow.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin")
@Validated
@Tag(name = "管理员模块", description = "管理员登录、仪表盘、用户管理、商家审核、商品管理和订单管理")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private ProductImageBackfillService productImageBackfillService;

    private boolean isAdminRequest(HttpServletRequest request) {
        Object userType = request.getAttribute("userType");
        return userType != null && "admin".equalsIgnoreCase(userType.toString());
    }

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "管理员使用用户名和密码登录，返回后台访问 Token")
    public Result<String> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        String token = adminService.login(username, password);
        return Result.success("登录成功", token);
    }

    /**
     * 获取当前管理员信息
     */
    @GetMapping("/current")
    @Operation(summary = "当前管理员信息", description = "根据当前登录态返回管理员资料")
    public Result<Admin> getCurrentAdmin(HttpServletRequest request) {
        if (!isAdminRequest(request)) {
            return Result.forbidden("仅管理员可查看当前信息");
        }
        Integer adminId = (Integer) request.getAttribute("userId");
        if (adminId == null) {
            return Result.unauthorized("管理员身份无效");
        }
        Admin admin = adminService.getCurrentAdmin(adminId);
        if (admin == null) {
            return Result.notFound("管理员不存在");
        }
        return Result.success(admin);
    }

    /**
     * 根据ID查询管理员
     */
    @GetMapping("/detail/{adminId}")
    public Result<Admin> getAdminById(@PathVariable @NotNull Integer adminId) {
        Admin admin = adminService.getAdminById(adminId);
        if (admin != null) {
            return Result.success(admin);
        } else {
            return Result.notFound("管理员不存在");
        }

    }

    /**
     * 根据用户名查询管理员
     */
    @GetMapping("/username/{username}")
    public Result<Admin> getAdminByUsername(@PathVariable @NotBlank String username) {
        Admin admin = adminService.getAdminByUsername(username);
        if (admin != null) {
            return Result.success(admin);
        } else {
            return Result.notFound("管理员不存在");
        }

    }

    /**
     * 查询所有管理员
     */
    @GetMapping("/list")
    public Result<List<Admin>> getAllAdmins() {
        List<Admin> admins = adminService.getAllAdmins();
        return Result.success(admins);

    }

    /**
     * 更新管理员信息
     */
    @PutMapping("/update")
    public Result<Void> updateAdmin(@Valid @RequestBody Admin admin) {
        boolean success = adminService.updateAdmin(admin);
        if (success) {
            return Result.success("更新成功", null);
        } else {
            return Result.error("更新失败");
        }

    }

    /**
     * 修改管理员密码
     */
    @PutMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, Object> passwordData) {
        Integer adminId = (Integer) passwordData.get("adminId");
        String oldPassword = (String) passwordData.get("oldPassword");
        String newPassword = (String) passwordData.get("newPassword");

        boolean success = adminService.changePassword(adminId, oldPassword, newPassword);
        if (success) {
            return Result.success("密码修改成功", null);
        } else {
            return Result.error("密码修改失败");
        }

    }

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/dashboard/stats")
    @Operation(summary = "管理后台统计", description = "返回用户数、商家数、待审核商家数、商品数和订单数")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 获取用户总数
        Integer userCount = userService.getUserCount();
        stats.put("userCount", userCount != null ? userCount : 0);

        // 获取商家总数
        Integer merchantCount = merchantService.getMerchantCount();
        stats.put("merchantCount", merchantCount != null ? merchantCount : 0);

        // 获取待审核商家数量
        Integer pendingMerchantCount = merchantService.getPendingMerchantCount();
        stats.put("pendingMerchantCount", pendingMerchantCount != null ? pendingMerchantCount : 0);

        // 获取商品总数
        Integer productCount = productService.getProductCountIncludeDeleted();
        stats.put("productCount", productCount != null ? productCount : 0);

        // 获取订单总数
        Integer orderCount = orderService.getOrderCount();
        stats.put("orderCount", orderCount != null ? orderCount : 0);

        return Result.success(stats);
    }

    /**
     * 提供测试数据（仅开发环境使用）
     */
    @GetMapping("/test-data")
    public Result<Void> generateTestData() {
        // 仅在开发环境执行
        if (!"dev".equals(System.getProperty("spring.profiles.active"))) {
            return Result.error("该接口仅在开发环境可用");
        }

        // TODO: 在实际项目中，应该删除此方法
        return Result.success("测试数据生成成功", null);
    }

    /**
     * 获取用户列表（分页）
     */
    @GetMapping("/users")
    @Operation(summary = "用户列表", description = "分页查询后台用户列表")
    public Result<Map<String, Object>> getUserList(
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "10") Integer size) {

        List<User> users = userService.getAllUsers(page, size);
        Integer total = userService.getUserCount();

        Map<String, Object> result = new HashMap<>();
        result.put("list", users);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);

        return Result.success(result);
    }

    /**
     * 搜索用户
     */
    @GetMapping("/users/search")
    @Operation(summary = "搜索用户", description = "按用户名、账号、手机号或邮箱搜索用户")
    public Result<List<User>> searchUsers(@RequestParam String keyword) {
        User searchUser = new User();

        // 根据关键词设置搜索条件
        if (keyword.matches("^1[3-9]\\d{9}$")) {
            // 手机号格式
            searchUser.setPhone(keyword);
        } else if (keyword.contains("@")) {
            // 邮箱格式
            searchUser.setEmail(keyword);
        } else {
            // 其他情况按用户名或账号搜索
            searchUser.setUsername(keyword);
            searchUser.setAccount(keyword);
        }

        List<User> users = userService.getUsersByCondition(searchUser);
        return Result.success(users);
    }

    /**
     * 获取商家列表（分页）
     */
    @GetMapping("/merchants")
    @Operation(summary = "商家列表", description = "分页查询商家信息")
    public Result<Map<String, Object>> getMerchantList(
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "10") Integer size) {

        List<Merchant> merchants = merchantService.getMerchantsByPage(page, size);
        Integer total = merchantService.getMerchantCount();

        Map<String, Object> result = new HashMap<>();
        result.put("list", merchants);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);

        return Result.success(result);
    }

    /**
     * 审核商家
     */
    @PutMapping("/merchants/review")
    @Operation(summary = "审核商家", description = "管理员审核商家，支持通过和拒绝")
    public Result<Void> reviewMerchant(@RequestBody Map<String, Object> reviewData) {
        String merchantId = (String) reviewData.get("merchantId");
        String statusStr = (String) reviewData.get("status");
        String reason = (String) reviewData.get("reason");

        MerchantStatus status = MerchantStatus.valueOf(statusStr);
        boolean success = merchantService.reviewMerchant(merchantId, status, reason);

        if (success) {
            return Result.success("商家审核成功", null);
        } else {
            return Result.error("商家审核失败");
        }
    }

    /**
     * 获取商品列表（分页）
     */
    @GetMapping("/products")
    @Operation(summary = "商品列表", description = "分页查询后台商品列表，包含已逻辑删除商品")
    public Result<Map<String, Object>> getProductList(
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "10") Integer size) {

        List<Product> products = productService.getProductsByPageIncludeDeleted(page, size);
        Integer total = productService.getProductCountIncludeDeleted();

        Map<String, Object> result = new HashMap<>();
        result.put("list", products);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);

        return Result.success(result);
    }

    /**
     * 获取订单列表（分页）
     */
    @GetMapping("/orders")
    @Operation(summary = "订单列表", description = "分页查询后台订单，可按状态筛选")
    public Result<Map<String, Object>> getOrderList(
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "订单状态，可选")
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        if (!isAdminRequest(request)) {
            return Result.forbidden("仅管理员可查看订单列表");
        }

        List<Order> orders;
        Integer total;
        if (status != null && !status.trim().isEmpty()) {
            OrderStatus orderStatus;
            try {
                orderStatus = OrderStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return Result.badRequest("无效的订单状态");
            }
            orders = orderService.getOrdersByStatusWithPage(orderStatus, page, size);
            total = orderService.getOrderCountByStatus(orderStatus);
        } else {
            orders = orderService.getOrdersByPage(page, size);
            total = orderService.getOrderCount();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", orders);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);

        return Result.success(result);
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/orders/status")
    @Operation(summary = "更新订单状态", description = "管理员仅允许将订单更新为已取消")
    public Result<Void> updateOrderStatus(@RequestBody Map<String, Object> statusData,
                                          HttpServletRequest request) {
        if (!isAdminRequest(request)) {
            return Result.forbidden("仅管理员可操作订单");
        }
        Integer orderId = (Integer) statusData.get("orderId");
        String statusStr = (String) statusData.get("status");

        if (orderId == null || statusStr == null) {
            return Result.badRequest("参数错误");
        }
        OrderStatus status;
        try {
            status = OrderStatus.valueOf(statusStr);
        } catch (IllegalArgumentException ex) {
            return Result.badRequest("无效的订单状态");
        }
        if (status != OrderStatus.CANCELLED) {
            return Result.badRequest("管理员仅允许取消订单");
        }
        boolean success = orderService.updateOrderStatus(orderId, status);

        if (success) {
            return Result.success("订单状态更新成功", null);
        } else {
            return Result.error("订单状态更新失败");
        }
    }

    /**
     * 获取订单详情（管理员）
     */
    @GetMapping({"/orders/{orderId}", "/orders/detail/{orderId}"})
    @Operation(summary = "订单详情", description = "查询指定订单的订单头和订单项明细")
    public Result<Map<String, Object>> getOrderDetail(@PathVariable Integer orderId,
                                                      HttpServletRequest request) {
        if (!isAdminRequest(request)) {
            return Result.forbidden("仅管理员可查看订单详情");
        }
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return Result.notFound("订单不存在");
        }
        List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(orderId);
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("items", orderItems);
        return Result.success(result);
    }

    /**
     * 管理员取消订单
     */
    @PutMapping({"/orders/{orderId}/cancel", "/orders/cancel/{orderId}"})
    @Operation(summary = "取消订单", description = "管理员取消待支付或已支付订单")
    public Result<Void> cancelOrder(@PathVariable Integer orderId,
                                    HttpServletRequest request) {
        if (!isAdminRequest(request)) {
            return Result.forbidden("仅管理员可取消订单");
        }
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return Result.notFound("订单不存在");
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            return Result.success("订单已取消", null);
        }
        if (order.getOrderStatus() != OrderStatus.PENDING && order.getOrderStatus() != OrderStatus.PAID) {
            return Result.badRequest("管理员仅可取消待支付或已支付订单");
        }
        boolean success = orderService.adminCancelOrder(orderId);
        if (success) {
            return Result.success("订单已取消", null);
        }
        return Result.error("取消订单失败");
    }

    @PutMapping("/products/status")
    @Operation(summary = "更新商品状态", description = "管理员更新商品上架、下架等状态")
    public Result<Void> updateProductStatus(@RequestBody Map<String, Object> statusData) {
        Integer productId = (Integer) statusData.get("productId");
        String statusStr = (String) statusData.get("status");
        if (productId == null || statusStr == null) {
            return Result.badRequest("参数错误");
        }
        ProductStatus status;
        try {
            status = ProductStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            return Result.badRequest("无效的商品状态");
        }
        boolean success = productService.updateProductStatus(productId, status);
        if (success) {
            return Result.success("商品状态更新成功", null);
        } else {
            return Result.error("商品状态更新失败");
        }
    }

    @PostMapping("/products/backfill-images")
    @Operation(summary = "商品图片回填", description = "将商品图片批量回填到 MinIO，用于后台维护")
    public Result<Map<String, Object>> backfillProductImages(
            @RequestParam(defaultValue = "true") boolean overwriteAll) {
        int updatedCount = productImageBackfillService.backfillProductImages(overwriteAll);
        Map<String, Object> result = new HashMap<>();
        result.put("updatedCount", updatedCount);
        result.put("overwriteAll", overwriteAll);
        return Result.success("商品图片已回填到MinIO", result);
    }
}
