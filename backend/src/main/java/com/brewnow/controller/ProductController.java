package com.brewnow.controller;

import com.brewnow.common.Result;
import com.brewnow.entity.Product;
import com.brewnow.service.BehaviorService;
import com.brewnow.service.ProductService;
import com.brewnow.service.RecommendationService;
import com.brewnow.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/product")
@Tag(name = "商品模块", description = "商城商品浏览、搜索、分类、热销和推荐")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private BehaviorService behaviorService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 查询所有商品（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "商品列表", description = "分页查询商城在售商品")
    public Result<List<Product>> getAllProducts(
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            List<Product> products = productService.getProductsByPage(page, size);
            return Result.success(products);
        } catch (Exception e) {
            return Result.error("查询商品列表失败");
        }
    }

    /**
     * 根据ID查询商品
     */
    @GetMapping("/{productId}")
    @Operation(summary = "商品详情", description = "按商品 ID 查询商品详情")
    public Result<Product> getProductById(@PathVariable Integer productId) {
        try {
            Product product = productService.getProductById(productId);
            if (product == null) {
                return Result.error("商品不存在");
            }
            return Result.success(product);
        } catch (Exception e) {
            return Result.error("查询商品详情失败");
        }
    }

    @GetMapping("/{productId}/detail")
    @Operation(summary = "商品详情并记录浏览", description = "查询商品详情，并在登录时记录浏览行为")
    public Result<Product> getProductByIdWithBehavior(@PathVariable Integer productId,
                                                      @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Product product = productService.getProductById(productId);
            if (product == null) {
                return Result.error("商品不存在");
            }
            Integer userId = resolveUserId(authHeader);
            if (userId != null) {
                behaviorService.recordBehavior(userId, productId, "VIEW");
            }
            return Result.success(product);
        } catch (Exception e) {
            return Result.error("查询商品详情失败");
        }
    }

    /**
     * 根据分类查询商品
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "分类商品", description = "按商品分类查询商品列表")
    public Result<List<Product>> getProductsByCategory(@PathVariable String category) {
        try {
            List<Product> products = productService.getProductsByCategory(category);
            return Result.success(products);
        } catch (Exception e) {
            return Result.error("查询分类商品失败");
        }
    }

    /**
     * 搜索商品
     */
    @GetMapping("/search")
    @Operation(summary = "商品搜索", description = "按关键词和分类搜索商品")
    public Result<List<Product>> searchProducts(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String category) {
        try {
            List<Product> products = productService.searchProducts(keyword, category);
            return Result.success(products);
        } catch (Exception e) {
            return Result.error("搜索商品失败");
        }
    }

    /**
     * 高级搜索商品
     */
    @GetMapping("/search/advanced")
    @Operation(summary = "高级搜索", description = "按关键词、分类、品牌、价格区间等条件搜索商品")
    public Result<List<Product>> searchProductsAdvanced(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String status) {
        try {
            // 这里可以扩展实现高级搜索，暂时使用关键词搜索
            List<Product> products;
            if (keyword != null && !keyword.trim().isEmpty()) {
                products = productService.searchProducts(keyword);
            } else if (category != null && !category.trim().isEmpty()) {
                products = productService.getProductsByCategory(category);
            } else {
                products = productService.getAllProducts();
            }
            return Result.success(products);
        } catch (Exception e) {
            return Result.error("高级搜索失败");
        }
    }

    /**
     * 获取热销商品
     */
    @GetMapping("/hot")
    @Operation(summary = "热销商品", description = "获取商城热销商品列表")
    public Result<List<Product>> getHotProducts(@RequestParam(defaultValue = "8") Integer limit) {
        try {
            List<Product> products = productService.getHotProducts(limit);
            return Result.success(products);
        } catch (Exception e) {
            return Result.error("获取热销商品失败");
        }
    }

    /**
     * 获取推荐商品
     */
    @GetMapping("/recommended")
    @Operation(summary = "推荐商品", description = "获取首页推荐商品列表")
    public Result<List<Product>> getRecommendedProducts(@RequestParam(defaultValue = "4") Integer limit) {
        try {
            List<Product> products = recommendationService.getHomeRecommendations(null, limit);
            return Result.success(products);
        } catch (Exception e) {
            return Result.error("获取推荐商品失败");
        }
    }

    /**
     * 获取商品分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "商品分类", description = "获取商品分类列表")
    public Result<List<String>> getCategories() {
        try {
            // 暂时返回固定分类，后续可以从数据库动态获取
            List<String> categories = Arrays.asList("茶叶", "茶具", "乌龙茶", "白茶", "花茶", "红茶", "绿茶");
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error("获取商品分类失败");
        }
    }

    /**
     * 添加商品
     */
    @PostMapping("/add")
    public Result<Void> addProduct(@RequestBody Product product) {
        return Result.forbidden("不支持该操作，请在商家端添加商品");
    }

    /**
     * 更新商品
     */
    @PutMapping("/update")
    public Result<Void> updateProduct(@RequestBody Product product) {
        return Result.forbidden("不支持该操作，请在商家端更新商品");
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{productId}")
    public Result<Void> deleteProduct(@PathVariable Integer productId) {
        return Result.error("不支持该操作，请在商家端删除商品");
    }

    private Integer resolveUserId(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return null;
            }
            return jwtUtil.getUserIdFromToken(authHeader.substring(7));
        } catch (Exception e) {
            return null;
        }
    }
}
