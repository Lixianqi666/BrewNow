import request from './request'

// 购物车项类型定义
export interface CartItem {
  cartItemId: number
  cartId: number
  productId: number
  quantity: number
  addTime: string
  product: {
    productId: number
    productName: string
    brand: string
    category: string
    price: number
    stockQuantity: number
    imageUrl: string
    status: string
  }
}

// 购物车API
export const cartApi = {
  // 添加商品到购物车
  addToCart(productId: number, quantity: number) {
    return request.post('/cart/add', {
      productId,
      quantity
    })
  },

  // 获取购物车列表
  getCartItems() {
    return request.get<CartItem[]>('/cart/list')
  },

  // 更新购物车商品数量
  updateCartItemQuantity(cartItemId: number, quantity: number) {
    return request.put('/cart/update', {
      cartItemId,
      quantity
    })
  },

  // 从购物车删除商品
  removeFromCart(cartItemId: number) {
    return request.delete('/cart/remove', {
      data: { cartItemId }
    })
  },

  // 清空购物车
  clearCart() {
    return request.delete('/cart/clear')
  },

  // 获取购物车商品数量
  getCartItemCount() {
    return request.get<number>('/cart/count')
  }
} 