package com.brewnow.controller;

import com.brewnow.common.Result;
import com.brewnow.entity.Merchant;
import com.brewnow.entity.Order;
import com.brewnow.entity.Product;
import com.brewnow.dto.order.MerchantOrderSummary;
import com.brewnow.enums.OrderStatus;
import com.brewnow.mapper.OrderMapper;
import com.brewnow.service.MinioStorageService;
import com.brewnow.service.MerchantService;
import com.brewnow.service.ProductService;
import com.brewnow.service.OrderService;
import com.brewnow.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 商家控制器
 */
@Slf4j
@RestController
@RequestMapping("/merchant")
@Tag(name = "商家模块", description = "商家后台统计、商品管理、订单管理、商家资料和图片上传")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MinioStorageService minioStorageService;

    /**
     * 获取商家后台统计数据
     */
    @GetMapping("/dashboard/stats")
    @Operation(summary = "商家后台统计", description = "返回商品数、订单数、低库存数量等商家工作台数据")
    public Result<Map<String, Object>> getDashboardStats(HttpServletRequest request) {
        try {
            String merchantId = getMerchantIdFromRequest(request);
            if (merchantId == null) {
                return Result.error("未找到商家信息");
            }

            Map<String, Object> stats = new HashMap<>();

            // 获取商品总数
            Integer productCount = productService.getProductCountByMerchant(merchantId);
            stats.put("productCount", productCount != null ? productCount : 0);

            // 获取订单总数
            Integer orderCount = orderService.getOrderCountByMerchant(merchantId);
            stats.put("orderCount", orderCount != null ? orderCount : 0);

            // 获取总收入（暂时设为0，后续可以实现）
            stats.put("totalRevenue", 0.0);

            // 获取客户数量（暂时设为0，后续可以实现）
            stats.put("customerCount", 0);

            Integer lowStockCount = productService.getLowStockCountByMerchant(merchantId);
            stats.put("lowStockCount", lowStockCount != null ? lowStockCount : 0);
            stats.put("lowStockProducts", productService.getLowStockProductsByMerchant(merchantId, 6));

            log.info("商家{}获取统计数据成功", merchantId);
            return Result.success("获取统计数据成功", stats);
        } catch (Exception e) {
            log.error("获取商家统计数据失败", e);
            return Result.error("获取统计数据失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询商家商品
     */
    @GetMapping("/products")
    @Operation(summary = "商家商品列表", description = "分页查询当前商家的商品，支持关键词和分类筛选")
    public Result<Map<String, Object>> getMerchantProducts(
            HttpServletRequest request,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "关键词，可按商品名、品牌或标签匹配")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "商品分类")
            @RequestParam(required = false) String category) {
        try {
            String merchantId = getMerchantIdFromRequest(request);
            if (merchantId == null) {
                return Result.error("未找到商家信息");
            }

            List<Product> products = productService.getMerchantProductsByPage(merchantId, page, size, keyword,
                    category);
            // 茶企/品牌统一：商家端只展示当前茶企的品牌名（短名）
            String brandName = resolveMerchantBrandName(merchantId);
            products.forEach(p -> p.setBrand(brandName));
            Integer total = productService.getMerchantProductCount(merchantId, keyword, category);

            Map<String, Object> result = new HashMap<>();
            result.put("list", products);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);

            log.info("商家{}获取商品列表成功，页码：{}，数量：{}", merchantId, page, products.size());
            return Result.success("获取商品列表成功", result);
        } catch (Exception e) {
            log.error("获取商家商品列表失败", e);
            return Result.error("获取商品列表失败：" + e.getMessage());
        }
    }

    /**
     * 添加商品
     */
    @PostMapping("/products")
    @Operation(summary = "添加商品", description = "商家新增茶叶商品，支持结构化属性和图片信息")
    public Result<Void> addProduct(HttpServletRequest request, @RequestBody Product product) {
        try {
            String merchantId = getMerchantIdFromRequest(request);
            if (merchantId == null) {
                return Result.error("未找到商家信息");
            }

            // 设置商家ID
            product.setMerchantId(merchantId);
            fillMerchantBrand(product, merchantId);

            boolean success = productService.addProduct(product);
            if (success) {
                log.info("商家{}添加商品成功：{}", merchantId, product.getProductName());
                return Result.success("商品添加成功", null);
            } else {
                return Result.error("商品添加失败");
            }
        } catch (Exception e) {
            log.error("添加商品失败", e);
            return Result.error("添加商品失败：" + e.getMessage());
        }
    }

    /**
     * 更新商品
     */
    @PutMapping("/products/{productId}")
    @Operation(summary = "更新商品", description = "商家更新自己名下的商品信息")
    public Result<Void> updateProduct(
            HttpServletRequest request,
            @PathVariable Integer productId,
            @RequestBody Product product) {
        try {
            String merchantId = getMerchantIdFromRequest(request);
            if (merchantId == null) {
                return Result.error("未找到商家信息");
            }

            // 验证商品是否属于该商家
            Product existingProduct = productService.getProductById(productId);
            if (existingProduct == null) {
                return Result.error("商品不存在");
            }
            if (!sameMerchant(merchantId, existingProduct.getMerchantId())) {
                return Result.error("无权操作此商品");
            }

            // 设置商品ID和商家ID
            product.setProductId(productId);
            product.setMerchantId(merchantId);
            fillMerchantBrand(product, merchantId);

            boolean success = productService.updateProduct(product);
            if (success) {
                log.info("商家{}更新商品成功：{}", merchantId, product.getProductName());
                return Result.success("商品更新成功", null);
            } else {
                return Result.error("商品更新失败");
            }
        } catch (Exception e) {
            log.error("更新商品失败", e);
            return Result.error("更新商品失败：" + e.getMessage());
        }
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/products/{productId}")
    @Operation(summary = "删除商品", description = "商家删除自己名下商品，历史订单不受影响")
    public Result<Void> deleteProduct(HttpServletRequest request, @PathVariable Integer productId) {
        try {
            String merchantId = getMerchantIdFromRequest(request);
            if (merchantId == null) {
                return Result.error("未找到商家信息");
            }

            boolean success = productService.deleteProduct(productId, merchantId);
            if (success) {
                log.info("商家{}删除商品成功：{}", merchantId, productId);
                return Result.success("商品已删除", null);
            } else {
                Product existingProduct = productService.getProductByIdIncludeDeleted(productId);
                if (existingProduct == null) {
                    return Result.error("商品不存在");
                }

                String ownerId = existingProduct.getMerchantId();
                if (!sameMerchant(merchantId, ownerId)) {
                    return Result.error("无权操作此商品");
                }

                if (existingProduct.getIsDeleted() != null && existingProduct.getIsDeleted() == 1) {
                    return Result.success("商品已删除", null);
                }

                return Result.error("商品删除失败");
            }
        } catch (Exception e) {
            log.error("删除商品失败", e);
            return Result.error("删除商品失败：" + e.getMessage());
        }
    }

    @GetMapping("/profile/current")
    @Operation(summary = "当前商家资料", description = "返回当前登录商家的基础资料")
    public Result<Merchant> getCurrentMerchantProfile(HttpServletRequest request) {
        try {
            String merchantId = getMerchantIdFromRequest(request);
            if (merchantId == null) {
                return Result.error("未找到商家信息");
            }

            Merchant merchant = merchantService.getMerchantById(merchantId);
            if (merchant == null) {
                return Result.error("商家信息不存在");
            }

            return Result.success(merchant);
        } catch (Exception e) {
            log.error("获取当前商家资料失败", e);
            return Result.error("获取当前商家资料失败：" + e.getMessage());
        }
    }

    /**
     * 商家订单列表
     */
    @GetMapping("/orders")
    @Operation(summary = "商家订单列表", description = "分页查询当前商家相关订单，可按状态筛选")
    public Result<Map<String, Object>> getMerchantOrders(
            HttpServletRequest request,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "订单状态，可选")
            @RequestParam(required = false) String status) {
        try {
            String merchantId = getMerchantIdFromRequest(request);
            if (merchantId == null) {
                return Result.error("未找到商家信息");
            }

            List<MerchantOrderSummary> orders = orderService.getOrdersByMerchant(merchantId, status, page, size);
            Integer total = orderService.getOrderCountByMerchant(merchantId, status);

            Map<String, Object> result = new HashMap<>();
            result.put("list", orders);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取商家订单失败", e);
            return Result.error("获取商家订单失败：" + e.getMessage());
        }
    }

    @GetMapping("/orders/{orderId}")
    @Operation(summary = "商家订单详情", description = "查询当前商家可见的订单详情")
    public Result<MerchantOrderSummary> getMerchantOrderDetail(HttpServletRequest request, @PathVariable Integer orderId) {
        try {
            String merchantId = getMerchantIdFromRequest(request);
            if (merchantId == null) {
                return Result.error("未找到商家信息");
            }

            Integer owned = orderMapper.countMerchantOrderOwnership(merchantId, orderId);
            if (owned == null || owned == 0) {
                return Result.error("无权查看该订单");
            }

            List<MerchantOrderSummary> orders = orderService.getOrdersByMerchant(merchantId, null, 1, 200);
            MerchantOrderSummary target = orders.stream()
                    .filter(order -> orderId.equals(order.getOrderId()))
                    .findFirst()
                    .orElse(null);
            if (target == null) {
                return Result.error("订单不存在");
            }

            Order order = orderService.getOrderById(orderId);
            target.setShippingAddress(order.getShippingAddress());
            target.setContactPhone(order.getContactPhone());
            target.setRemark(order.getRemark());

            return Result.success(target);
        } catch (Exception e) {
            log.error("获取商家订单详情失败", e);
            return Result.error("获取商家订单详情失败：" + e.getMessage());
        }
    }

    @PutMapping("/orders/{orderId}/ship")
    @Operation(summary = "订单发货", description = "商家将已支付订单标记为已发货")
    public Result<Void> shipMerchantOrder(HttpServletRequest request, @PathVariable Integer orderId) {
        try {
            String merchantId = getMerchantIdFromRequest(request);
            if (merchantId == null) {
                return Result.error("未找到商家信息");
            }

            Integer owned = orderMapper.countMerchantOrderOwnership(merchantId, orderId);
            if (owned == null || owned == 0) {
                return Result.error("无权操作该订单");
            }

            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return Result.error("订单不存在");
            }
            if (order.getOrderStatus() != OrderStatus.PAID) {
                return Result.error("仅已支付订单可标记发货");
            }

            boolean success = orderService.updateOrderStatus(orderId, OrderStatus.SHIPPED);
            if (!success) {
                return Result.error("订单发货失败");
            }
            return Result.success("订单已标记发货", null);
        } catch (Exception e) {
            log.error("商家订单发货失败", e);
            return Result.error("订单发货失败：" + e.getMessage());
        }
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/products/{productId}")
    @Operation(summary = "商家商品详情", description = "查询当前商家名下商品详情")
    public Result<Product> getProductDetail(HttpServletRequest request, @PathVariable Integer productId) {
        try {
            String merchantId = getMerchantIdFromRequest(request);
            if (merchantId == null) {
                return Result.error("未找到商家信息");
            }

            Product product = productService.getProductById(productId);
            if (product == null) {
                return Result.error("商品不存在");
            }
            if (!sameMerchant(merchantId, product.getMerchantId())) {
                return Result.error("无权查看此商品");
            }

            product.setBrand(resolveMerchantBrandName(merchantId));
            log.info("商家{}获取商品详情成功：{}", merchantId, productId);
            return Result.success("获取商品详情成功", product);
        } catch (Exception e) {
            log.error("获取商品详情失败", e);
            return Result.error("获取商品详情失败：" + e.getMessage());
        }
    }

    /**
     * 更新商品状态
     */
    @PutMapping("/products/{productId}/status")
    @Operation(summary = "更新商品状态", description = "商家对自己商品进行上架或下架")
    public Result<Void> updateProductStatus(
            HttpServletRequest request,
            @PathVariable Integer productId,
            @RequestParam String status) {
        try {
            String merchantId = getMerchantIdFromRequest(request);
            if (merchantId == null) {
                return Result.error("未找到商家信息");
            }

            // 验证商品是否属于该商家
            Product existingProduct = productService.getProductById(productId);
            if (existingProduct == null) {
                return Result.error("商品不存在");
            }
            if (!sameMerchant(merchantId, existingProduct.getMerchantId())) {
                return Result.error("无权操作此商品");
            }

            boolean success = productService.updateProductStatus(productId, status);
            if (success) {
                log.info("商家{}更新商品状态成功：{} -> {}", merchantId, productId, status);
                return Result.success("商品状态更新成功", null);
            } else {
                return Result.error("商品状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新商品状态失败", e);
            return Result.error("更新商品状态失败：" + e.getMessage());
        }
    }

    /**
     * 上传商品图片
     */
    @PostMapping(value = "/products/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传商品图片", description = "上传商品图片到 MinIO 并返回可访问地址")
    public Result<Map<String, String>> uploadProductImage(
            HttpServletRequest request,
            @Parameter(description = "图片文件")
            @RequestParam("file") MultipartFile file) {
        try {
            String merchantId = getMerchantIdFromRequest(request);
            if (merchantId == null) {
                return Result.error("未找到商家信息");
            }

            if (file == null || file.isEmpty()) {
                return Result.error("请上传图片文件");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("仅支持图片文件");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = ".jpg";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }

            String filename = "product_" + merchantId + "_" + UUID.randomUUID().toString().replace("-", "") + extension;
            String imageUrl = minioStorageService.uploadImage(file, "products", filename);
            Map<String, String> result = new HashMap<>();
            result.put("imageUrl", imageUrl);
            return Result.success("商品图片上传成功", result);
        } catch (Exception e) {
            log.error("上传商品图片失败", e);
            return Result.error("商品图片上传失败：" + e.getMessage());
        }
    }

    /**
     * 从请求中获取商家ID
     */
    private String getMerchantIdFromRequest(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Authorization header missing or invalid");
                return null;
            }

            String token = authHeader.substring(7);
            String userType = jwtUtil.getUserTypeFromToken(token);

            // 验证用户类型
            if (!"user".equals(userType)) {
                log.warn("User type is not 'user': {}", userType);
                return null;
            }

            Integer userId = jwtUtil.getUserIdFromToken(token);
            if (userId != null) {
                Merchant merchant = merchantService.getMerchantByUserId(userId);
                if (merchant != null && merchant.getMerchantId() != null && !merchant.getMerchantId().trim().isEmpty()) {
                    return merchant.getMerchantId().trim();
                }
            }

            String merchantId = jwtUtil.getMerchantIdFromToken(token);
            if (merchantId == null || merchantId.trim().isEmpty()) {
                log.warn("Merchant ID not found in token");
                return null;
            }

            return merchantId;
        } catch (Exception e) {
            log.error("Failed to get merchant ID from request", e);
            return null;
        }
    }

    private void fillMerchantBrand(Product product, String merchantId) {
        product.setBrand(resolveMerchantBrandName(merchantId));
    }

    private boolean sameMerchant(String a, String b) {
        if (a == null || b == null) return false;
        return a.trim().equals(b.trim());
    }

    private String resolveMerchantBrandName(String merchantId) {
        Merchant merchant = merchantService.getMerchantById(merchantId);
        String companyName = merchant != null ? merchant.getCompanyName() : null;
        if (companyName == null) return merchantId;
        String trimmed = companyName.trim();
        if (trimmed.isEmpty()) return merchantId;

        // 常见企业后缀去除：让“沏刻茶业有限公司”显示为“沏刻”
        String[] suffixes = new String[] {
                "茶业有限责任公司",
                "茶叶有限责任公司",
                "茶业有限公司",
                "茶叶有限公司",
                "有限责任公司",
                "有限公司"
        };
        for (String suffix : suffixes) {
            if (trimmed.endsWith(suffix)) {
                String candidate = trimmed.substring(0, trimmed.length() - suffix.length()).trim();
                if (!candidate.isEmpty()) return candidate;
                break;
            }
        }
        return trimmed;
    }
}
