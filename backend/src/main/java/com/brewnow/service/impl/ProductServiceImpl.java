package com.brewnow.service.impl;

import com.brewnow.entity.Product;
import com.brewnow.enums.ProductStatus;
import com.brewnow.mapper.ProductMapper;
import com.brewnow.service.ProductService;
import com.brewnow.service.RecommendationCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品服务实现类
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RecommendationCacheService recommendationCacheService;

    @Override
    public Product getProductById(Integer productId) {
        return productMapper.selectById(productId);
    }

    @Override
    public Product getProductByIdIncludeDeleted(Integer productId) {
        return productMapper.selectByIdIncludeDeleted(productId);
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        return productMapper.selectByCategory(category);
    }

    @Override
    public List<Product> getAllProducts() {
        return productMapper.selectAll();
    }

    @Override
    public List<Product> getProductsByPage(int page, int size) {
        int offset = (page - 1) * size;
        return productMapper.selectByPage(offset, size);
    }

    @Override
    public List<Product> getProductsByPageIncludeDeleted(int page, int size) {
        int offset = (page - 1) * size;
        return productMapper.selectByPageIncludeDeleted(offset, size);
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        return productMapper.search(keyword);
    }

    @Override
    public List<Product> searchProducts(String keyword, String category) {
        return productMapper.searchWithCategory(keyword, category);
    }

    @Override
    @Transactional
    public boolean addProduct(Product product) {
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        product.setStatus(ProductStatus.INACTIVE); // 默认为下架状态
        if (product.getWarningStock() == null || product.getWarningStock() < 0) {
            int fallback = 10;
            if (product.getStockQuantity() != null && product.getStockQuantity() > 0) {
                fallback = Math.max(1, Math.min(10, product.getStockQuantity()));
            }
            product.setWarningStock(fallback);
        }
        boolean success = productMapper.insert(product) > 0;
        if (success) {
            recommendationCacheService.evictRecommendationCaches(null, product.getProductId());
            recommendationCacheService.evictHotAndCategoryCaches();
        }
        return success;
    }

    @Override
    @Transactional
    public boolean updateProduct(Product product) {
        product.setUpdateTime(LocalDateTime.now());
        boolean success = productMapper.updateById(product) > 0;
        if (success) {
            recommendationCacheService.evictRecommendationCaches(null, product.getProductId());
            recommendationCacheService.evictHotAndCategoryCaches();
        }
        return success;
    }

    @Override
    @Transactional
    public boolean updateProductStatus(Integer productId, ProductStatus status) {
        Product product = getProductById(productId);
        if (product == null) {
            return false;
        }

        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());

        return updateProduct(product);
    }

    @Override
    @Transactional
    public boolean deleteProduct(Integer productId, String merchantId) {
        boolean success = productMapper.softDeleteByIdAndMerchant(productId, merchantId) > 0;
        if (success) {
            recommendationCacheService.evictRecommendationCaches(null, productId);
            recommendationCacheService.evictHotAndCategoryCaches();
        }
        return success;
    }

    @Override
    @Transactional
    public boolean deductStock(Integer productId, Integer quantity) {
        if (productId == null || quantity == null || quantity <= 0) {
            return false;
        }
        return productMapper.deductStock(productId, quantity) > 0;
    }

    @Override
    @Transactional
    public boolean restoreStock(Integer productId, Integer quantity) {
        if (productId == null || quantity == null || quantity <= 0) {
            return false;
        }
        return productMapper.restoreStock(productId, quantity) > 0;
    }

    @Override
    public Integer getProductCount() {
        return productMapper.countAll();
    }

    @Override
    public Integer getProductCountIncludeDeleted() {
        return productMapper.countAllIncludeDeleted();
    }

    @Override
    @Cacheable(cacheNames = "product:hot", key = "'hot:' + #limit")
    public List<Product> getHotProducts(int limit) {
        return productMapper.selectHotProducts(limit);
    }

    @Override
    public long getTotalProductCount() {
        Integer count = productMapper.countAll();
        return count != null ? count.longValue() : 0L;
    }

    // =============== 商家专用方法实现 ===============

    @Override
    public List<Product> getMerchantProductsByPage(String merchantId, int page, int size, String keyword,
            String category) {
        int offset = (page - 1) * size;
        return productMapper.selectByMerchantWithPage(merchantId, offset, size, keyword, category);
    }

    @Override
    public Integer getMerchantProductCount(String merchantId, String keyword, String category) {
        return productMapper.countByMerchant(merchantId, keyword, category);
    }

    @Override
    public Integer getProductCountByMerchant(String merchantId) {
        return productMapper.countByMerchant(merchantId, null, null);
    }

    @Override
    public List<Product> getProductsByMerchant(String merchantId) {
        return productMapper.selectByMerchant(merchantId);
    }

    @Override
    @Transactional
    public boolean updateProductStatus(Integer productId, String status) {
        return productMapper.updateProductStatus(productId, status) > 0;
    }

    @Override
    public Integer getLowStockCountByMerchant(String merchantId) {
        return productMapper.countLowStockByMerchant(merchantId);
    }

    @Override
    public List<Product> getLowStockProductsByMerchant(String merchantId, int limit) {
        return productMapper.selectLowStockByMerchant(merchantId, limit);
    }
}
