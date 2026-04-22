package com.brewnow.service;

import com.brewnow.entity.Product;

import java.util.List;

public interface FavoriteService {

    boolean toggleFavorite(Integer userId, Integer productId);

    boolean isFavorite(Integer userId, Integer productId);

    List<Product> getFavoriteProducts(Integer userId);

    Integer getActiveFavoriteCount();
}
