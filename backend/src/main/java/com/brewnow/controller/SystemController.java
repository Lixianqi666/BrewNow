package com.brewnow.controller;

import com.brewnow.common.Result;
import com.brewnow.entity.Product;
import com.brewnow.service.ProductService;
import com.brewnow.service.UserService;
import com.brewnow.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统控制器
 */
@RestController
@RequestMapping("/system")
@Tag(name = "系统模块", description = "系统信息、健康检查、运行统计与接口文档入口")
public class SystemController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "返回后端服务运行状态、时间戳和版本信息")
    public Result<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("application", "brew-now-backend");
        healthInfo.put("version", "1.0.0");

        return Result.success(healthInfo);
    }

    /**
     * 系统信息
     */
    @GetMapping("/info")
    @Operation(summary = "系统信息", description = "返回系统名称、版本、运行环境等基础信息")
    public Result<Map<String, Object>> info() {
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("applicationName", "沏刻茶叶电商平台");
        systemInfo.put("version", "1.0.0");
        systemInfo.put("description", "基于SpringBoot构建的茶叶电商平台");
        systemInfo.put("author", "沏刻茶叶电商平台项目组");
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("springBootVersion", "2.7.18");
        systemInfo.put("buildTime", LocalDateTime.now());

        return Result.success(systemInfo);
    }

    /**
     * API文档地址
     */
    @GetMapping("/docs")
    @Operation(summary = "接口文档入口", description = "返回 Swagger/OpenAPI 文档访问地址")
    public Result<Map<String, String>> docs() {
        Map<String, String> docs = new HashMap<>();
        docs.put("swaggerUi", "/api/swagger-ui/index.html");
        docs.put("openApiJson", "/api/v3/api-docs");
        docs.put("apiPrefix", "/api");
        docs.put("userApis", "/api/user/*");
        docs.put("productApis", "/api/product/*");
        docs.put("systemApis", "/api/system/*");

        return Result.success(docs);
    }

    /**
     * 获取热销推荐商品
     */
    @GetMapping("/hot-products")
    @Operation(summary = "热销商品", description = "返回用于首页或系统展示的热销商品列表")
    public Result<List<Product>> getHotProducts(@RequestParam(defaultValue = "8") int limit) {
        try {
            List<Product> hotProducts = productService.getHotProducts(limit);
            return Result.success(hotProducts);
        } catch (Exception e) {
            return Result.error("获取热销商品失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "系统统计", description = "返回用户、商品、订单及今日新增数据统计")
    public Result<Map<String, Object>> getSystemStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 获取商品总数
            long totalProducts = productService.getTotalProductCount();
            stats.put("totalProducts", totalProducts);

            // 获取用户总数
            long totalUsers = userService.getTotalUserCount();
            stats.put("totalUsers", totalUsers);

            // 获取订单总数
            long totalOrders = orderService.getTotalOrderCount();
            stats.put("totalOrders", totalOrders);

            // 获取今日新增数据
            long todayNewUsers = userService.getTodayNewUserCount();
            stats.put("todayNewUsers", todayNewUsers);

            long todayNewOrders = orderService.getTodayNewOrderCount();
            stats.put("todayNewOrders", todayNewOrders);

            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取系统统计失败: " + e.getMessage());
        }
    }
}
