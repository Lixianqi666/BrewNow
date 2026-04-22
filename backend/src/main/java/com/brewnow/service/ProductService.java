package com.brewnow.service;

import com.brewnow.entity.Product;
import com.brewnow.enums.ProductStatus;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 根据ID查询商品
     */
    Product getProductById(Integer productId);

    /**
     * 根据ID查询商品（包含已删除）
     */
    Product getProductByIdIncludeDeleted(Integer productId);

    /**
     * 根据分类查询商品
     */
    List<Product> getProductsByCategory(String category);

    /**
     * 查询所有商品
     */
    List<Product> getAllProducts();

    /**
     * 分页查询商品
     */
    List<Product> getProductsByPage(int page, int size);

    /**
     * 分页查询商品（包含已删除）
     */
    List<Product> getProductsByPageIncludeDeleted(int page, int size);

    /**
     * 搜索商品
     */
    List<Product> searchProducts(String keyword);

    /**
     * 关键词和分类组合搜索
     */
    List<Product> searchProducts(String keyword, String category);

    /**
     * 添加商品
     */
    boolean addProduct(Product product);

    /**
     * 更新商品
     */
    boolean updateProduct(Product product);

    /**
     * 更新商品状态
     */
    boolean updateProductStatus(Integer productId, ProductStatus status);

    /**
     * 删除商品
     */
    boolean deleteProduct(Integer productId, String merchantId);

    /**
     * 扣减库存
     */
    boolean deductStock(Integer productId, Integer quantity);

    /**
     * 回补库存
     */
    boolean restoreStock(Integer productId, Integer quantity);

    /**
     * 获取商品总数
     */
    Integer getProductCount();

    /**
     * 获取商品总数（包含已删除）
     */
    Integer getProductCountIncludeDeleted();

    /**
     * 获取热销商品
     */
    List<Product> getHotProducts(int limit);

    /**
     * 获取商品总数（用于统计）
     */
    long getTotalProductCount();

    // =============== 商家专用方法 ===============

    /**
     * 根据商家ID分页查询商品
     * 
     * @param merchantId 商家ID
     * @param page       页码
     * @param size       每页数量
     * @param keyword    关键词（可选）
     * @param category   分类（可选）
     * @return 商品列表
     */
    List<Product> getMerchantProductsByPage(String merchantId, int page, int size, String keyword, String category);

    /**
     * 根据商家ID统计商品数量
     * 
     * @param merchantId 商家ID
     * @param keyword    关键词（可选）
     * @param category   分类（可选）
     * @return 商品数量
     */
    Integer getMerchantProductCount(String merchantId, String keyword, String category);

    /**
     * 根据商家ID获取商品总数
     * 
     * @param merchantId 商家ID
     * @return 商品总数
     */
    Integer getProductCountByMerchant(String merchantId);

    /**
     * 根据商家ID查询所有商品
     * 
     * @param merchantId 商家ID
     * @return 商品列表
     */
    List<Product> getProductsByMerchant(String merchantId);

    /**
     * 更新商品状态（字符串版本）
     * 
     * @param productId 商品ID
     * @param status    状态字符串
     * @return 是否成功
     */
    boolean updateProductStatus(Integer productId, String status);

    /**
     * 统计商家低库存商品
     */
    Integer getLowStockCountByMerchant(String merchantId);

    /**
     * 获取商家低库存商品
     */
    List<Product> getLowStockProductsByMerchant(String merchantId, int limit);
}
