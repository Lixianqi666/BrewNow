package com.brewnow.service.impl;

import com.brewnow.entity.Cart;
import com.brewnow.entity.CartItem;
import com.brewnow.mapper.CartItemMapper;
import com.brewnow.mapper.CartMapper;
import com.brewnow.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 购物车服务实现类
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartItemMapper cartItemMapper;

    @Override
    @Transactional
    public boolean addToCart(Integer userId, Integer productId, Integer quantity) {
        try {
            // 获取或创建用户购物车
            Cart cart = getOrCreateCart(userId);
            if (cart == null) {
                return false;
            }

            // 检查商品是否已在购物车中
            CartItem existingItem = cartItemMapper.selectByCartIdAndProductId(cart.getCartId(), productId);

            if (existingItem != null) {
                // 如果商品已存在，更新数量
                int newQuantity = existingItem.getQuantity() + quantity;
                return cartItemMapper.updateQuantity(existingItem.getCartItemId(), newQuantity) > 0;
            } else {
                // 如果商品不存在，添加新的购物车项
                CartItem cartItem = new CartItem(cart.getCartId(), productId, quantity);
                cartItem.setAddTime(LocalDateTime.now());
                return cartItemMapper.insert(cartItem) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<CartItem> getCartItems(Integer userId) {
        Cart cart = cartMapper.selectByUserId(userId);
        if (cart == null) {
            return Collections.emptyList();
        }
        return cartItemMapper.selectByCartId(cart.getCartId());
    }

    @Override
    @Transactional
    public boolean updateCartItemQuantity(Integer userId, Integer cartItemId, Integer quantity) {
        try {
            // 验证购物车项是否属于该用户
            if (!isCartItemOwnedByUser(userId, cartItemId)) {
                return false;
            }

            if (quantity <= 0) {
                // 如果数量为0或负数，删除该商品
                return cartItemMapper.deleteById(cartItemId) > 0;
            } else {
                // 更新数量
                return cartItemMapper.updateQuantity(cartItemId, quantity) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean removeFromCart(Integer userId, Integer cartItemId) {
        try {
            // 验证购物车项是否属于该用户
            if (!isCartItemOwnedByUser(userId, cartItemId)) {
                return false;
            }

            return cartItemMapper.deleteById(cartItemId) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean clearCart(Integer userId) {
        try {
            Cart cart = cartMapper.selectByUserId(userId);
            if (cart == null) {
                return true; // 购物车不存在，认为清空成功
            }

            return cartItemMapper.deleteByCartId(cart.getCartId()) >= 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Integer getCartItemCount(Integer userId) {
        Cart cart = cartMapper.selectByUserId(userId);
        if (cart == null) {
            return 0;
        }
        return cartItemMapper.countByCartId(cart.getCartId());
    }

    /**
     * 获取或创建用户购物车
     */
    private Cart getOrCreateCart(Integer userId) {
        Cart cart = cartMapper.selectByUserId(userId);
        if (cart == null) {
            // 创建新购物车
            cart = new Cart(userId);
            cart.setCreateTime(LocalDateTime.now());
            cart.setUpdateTime(LocalDateTime.now());

            if (cartMapper.insert(cart) > 0) {
                return cart;
            } else {
                return null;
            }
        }
        return cart;
    }

    /**
     * 验证购物车项是否属于指定用户
     */
    private boolean isCartItemOwnedByUser(Integer userId, Integer cartItemId) {
        Cart cart = cartMapper.selectByUserId(userId);
        if (cart == null) {
            return false;
        }

        List<CartItem> cartItems = cartItemMapper.selectByCartId(cart.getCartId());
        return cartItems.stream()
                .anyMatch(item -> item.getCartItemId().equals(cartItemId));
    }
}