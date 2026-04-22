package com.brewnow.mapper;

import com.brewnow.entity.Product;
import com.brewnow.enums.ProductStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品Mapper接口
 */
@Mapper
public interface ProductMapper {

    /**
     * 根据ID查询商品
     * 
     * @param productId 商品ID
     * @return 商品信息
     */
    Product selectById(@Param("productId") Integer productId);

    /**
     * 根据ID查询商品（包含已删除）
     *
     * @param productId 商品ID
     * @return 商品信息
     */
    Product selectByIdIncludeDeleted(@Param("productId") Integer productId);

    /**
     * 查询所有商品（分页）
     * 
     * @param offset 偏移量
     * @param limit  限制数量
     * @return 商品列表
     */
    List<Product> selectAll();

    /**
     * 根据条件查询商品
     * 
     * @param product 查询条件
     * @return 商品列表
     */
    List<Product> selectByCondition(Product product);

    /**
     * 根据分类查询商品
     * 
     * @param category 分类
     * @return 商品列表
     */
    List<Product> selectByCategory(@Param("category") String category);

    /**
     * 根据品牌查询商品
     * 
     * @param brand 品牌
     * @return 商品列表
     */
    List<Product> selectByBrand(String brand);

    /**
     * 根据价格范围查询商品
     * 
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @return 商品列表
     */
    List<Product> selectByPriceRange(@Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);

    /**
     * 搜索商品（根据商品名称）
     * 
     * @param keyword 关键词
     * @return 商品列表
     */
    List<Product> search(@Param("keyword") String keyword);

    /**
     * 按关键词和分类组合搜索
     */
    List<Product> searchWithCategory(@Param("keyword") String keyword, @Param("category") String category);

    /**
     * 获取热门商品（根据销量）
     * 
     * @param limit 限制数量
     * @return 商品列表
     */
    List<Product> selectHotProducts(Integer limit);

    /**
     * 统计商品总数
     * 
     * @return 商品总数
     */
    Integer countAll();

    /**
     * 统计商品总数（包含已删除）
     * 
     * @return 商品总数
     */
    Integer countAllIncludeDeleted();

    /**
     * 根据条件统计商品数量
     * 
     * @param product 查询条件
     * @return 商品数量
     */
    Integer countByCondition(Product product);

    /**
     * 根据分类统计商品数量
     * 
     * @param category 分类
     * @return 商品数量
     */
    Integer countByCategory(@Param("category") String category);

    /**
     * 插入商品
     * 
     * @param product 商品信息
     * @return 插入成功的记录数
     */
    Integer insert(Product product);

    /**
     * 根据ID更新商品
     * 
     * @param product 商品信息
     * @return 更新成功的记录数
     */
    Integer updateById(Product product);

    /**
     * 更新商品库存
     * 
     * @param productId 商品ID
     * @param quantity  库存变化量（正数增加，负数减少）
     * @return 更新成功的记录数
     */
    Integer updateStock(@Param("productId") Integer productId, @Param("quantity") Integer quantity);

    /**
     * 扣减库存，要求库存充足
     */
    Integer deductStock(@Param("productId") Integer productId, @Param("quantity") Integer quantity);

    /**
     * 回补库存
     */
    Integer restoreStock(@Param("productId") Integer productId, @Param("quantity") Integer quantity);

    /**
     * 逻辑删除商品（仅商家自己的商品）
     *
     * @param productId  商品ID
     * @param merchantId 商家ID
     * @return 更新成功的记录数
     */
    Integer softDeleteByIdAndMerchant(@Param("productId") Integer productId, @Param("merchantId") String merchantId);

    /**
     * 获取所有商品分类
     * 
     * @return 分类列表
     */
    List<String> selectAllCategories();

    /**
     * 获取所有商品品牌
     * 
     * @return 品牌列表
     */
    List<String> selectAllBrands();

    /**
     * 检查商品是否存在
     * 
     * @param productId 商品ID
     * @return 是否存在
     */
    boolean existsById(Integer productId);

    /**
     * 分页查询商品
     */
    List<Product> selectByPage(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 分页查询商品（包含已删除）
     */
    List<Product> selectByPageIncludeDeleted(@Param("offset") int offset, @Param("limit") int limit);

    // =============== 商家专用方法 ===============

    /**
     * 根据商家ID分页查询商品
     * 
     * @param merchantId 商家ID
     * @param offset     偏移量
     * @param limit      限制数量
     * @param keyword    关键词（可选）
     * @param category   分类（可选）
     * @return 商品列表
     */
    List<Product> selectByMerchantWithPage(@Param("merchantId") String merchantId,
            @Param("offset") int offset,
            @Param("limit") int limit,
            @Param("keyword") String keyword,
            @Param("category") String category);

    /**
     * 根据商家ID统计商品数量
     * 
     * @param merchantId 商家ID
     * @param keyword    关键词（可选）
     * @param category   分类（可选）
     * @return 商品数量
     */
    Integer countByMerchant(@Param("merchantId") String merchantId,
            @Param("keyword") String keyword,
            @Param("category") String category);

    /**
     * 根据商家ID查询所有商品
     * 
     * @param merchantId 商家ID
     * @return 商品列表
     */
    List<Product> selectByMerchant(@Param("merchantId") String merchantId);

    /**
     * 更新商品状态
     * 
     * @param productId 商品ID
     * @param status    状态
     * @return 更新成功的记录数
     */
    Integer updateProductStatus(@Param("productId") Integer productId, @Param("status") String status);

    /**
     * 仅更新商品图片地址
     */
    Integer updateImageUrlById(@Param("productId") Integer productId, @Param("imageUrl") String imageUrl);

    /**
     * 统计商家低库存商品数量
     */
    Integer countLowStockByMerchant(@Param("merchantId") String merchantId);

    /**
     * 查询商家低库存商品
     */
    List<Product> selectLowStockByMerchant(@Param("merchantId") String merchantId, @Param("limit") Integer limit);
}
