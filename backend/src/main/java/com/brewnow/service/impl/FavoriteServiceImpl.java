package com.brewnow.service.impl;

import com.brewnow.entity.Product;
import com.brewnow.entity.UserFavorite;
import com.brewnow.mapper.UserFavoriteMapper;
import com.brewnow.service.BehaviorService;
import com.brewnow.service.FavoriteService;
import com.brewnow.service.RecommendationCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Autowired
    private BehaviorService behaviorService;

    @Autowired
    private RecommendationCacheService recommendationCacheService;

    @Override
    @Transactional
    public boolean toggleFavorite(Integer userId, Integer productId) {
        UserFavorite favorite = userFavoriteMapper.selectByUserAndProduct(userId, productId);
        if (favorite == null) {
            UserFavorite newFavorite = new UserFavorite();
            newFavorite.setUserId(userId);
            newFavorite.setProductId(productId);
            newFavorite.setCreatedAt(LocalDateTime.now());
            newFavorite.setDeletedAt(null);
            boolean success = userFavoriteMapper.insert(newFavorite) > 0;
            if (success) {
                behaviorService.recordBehavior(userId, productId, "FAVORITE");
                recommendationCacheService.evictRecommendationCaches(userId, productId);
            }
            return success;
        }

        if (favorite.getDeletedAt() == null) {
            boolean success = userFavoriteMapper.softDelete(userId, productId) > 0;
            if (success) {
                recommendationCacheService.evictRecommendationCaches(userId, productId);
            }
            return success;
        }

        boolean success = userFavoriteMapper.restore(userId, productId) > 0;
        if (success) {
            behaviorService.recordBehavior(userId, productId, "FAVORITE");
            recommendationCacheService.evictRecommendationCaches(userId, productId);
        }
        return success;
    }

    @Override
    public boolean isFavorite(Integer userId, Integer productId) {
        return userFavoriteMapper.countActiveByUserAndProduct(userId, productId) > 0;
    }

    @Override
    public List<Product> getFavoriteProducts(Integer userId) {
        return userFavoriteMapper.selectActiveProductsByUser(userId);
    }

    @Override
    public Integer getActiveFavoriteCount() {
        return userFavoriteMapper.countAllActive();
    }
}
