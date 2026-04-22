import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { cartApi, type CartItem } from '@/api/cart'
import { useUserStore } from './user'

export const useCartStore = defineStore('cart', () => {
  // 状态
  const cartItems = ref<CartItem[]>([])
  const loading = ref(false)
  const lastUpdateTime = ref<number>(0)
  const cacheExpireTime = 5 * 60 * 1000 // 缓存5分钟过期

  // 计算属性
  const cartCount = computed(() => {
    return cartItems.value.reduce((total, item) => total + item.quantity, 0)
  })

  const cartTotal = computed(() => {
    return cartItems.value.reduce((total, item) => 
      total + (item.product.price * item.quantity), 0
    )
  })

  const isEmpty = computed(() => cartItems.value.length === 0)

  // 检查缓存是否过期
  const isCacheExpired = () => {
    return Date.now() - lastUpdateTime.value > cacheExpireTime
  }

  // 加载购物车数据
  const loadCartItems = async (forceRefresh = false) => {
    const userStore = useUserStore()
    
    // 如果用户未登录，清空购物车
    if (!userStore.isLoggedIn) {
      cartItems.value = []
      lastUpdateTime.value = 0
      return
    }

    // 如果缓存没过期且不是强制刷新，直接返回缓存数据
    if (!forceRefresh && !isCacheExpired() && cartItems.value.length > 0) {
      return cartItems.value
    }

    loading.value = true
    try {
      const response = await cartApi.getCartItems()
      if (response.data) {
        cartItems.value = response.data
        lastUpdateTime.value = Date.now()
      }
    } catch (error) {
      console.error('加载购物车失败:', error)
      // 不抛出错误，保持用户体验
    } finally {
      loading.value = false
    }

    return cartItems.value
  }

  // 添加商品到购物车
  const addToCart = async (productId: number, quantity: number = 1) => {
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) {
      throw new Error('请先登录')
    }

    try {
      await cartApi.addToCart(productId, quantity)
      
      // 检查是否已存在该商品
      const existingItemIndex = cartItems.value.findIndex(
        item => item.product.productId === productId
      )
      
      if (existingItemIndex !== -1) {
        // 更新现有商品数量
        cartItems.value[existingItemIndex].quantity += quantity
      } else {
        // 刷新购物车数据以获取完整的商品信息
        await loadCartItems(true)
      }
      
      lastUpdateTime.value = Date.now()
    } catch (error) {
      console.error('添加到购物车失败:', error)
      throw error
    }
  }

  // 更新商品数量
  const updateItemQuantity = async (cartItemId: number, quantity: number) => {
    if (quantity < 1) return

    try {
      await cartApi.updateCartItemQuantity(cartItemId, quantity)
      
      // 更新本地缓存
      const item = cartItems.value.find(item => item.cartItemId === cartItemId)
      if (item) {
        item.quantity = quantity
      }
      
      lastUpdateTime.value = Date.now()
    } catch (error) {
      console.error('更新商品数量失败:', error)
      throw error
    }
  }

  // 删除购物车商品
  const removeFromCart = async (cartItemId: number) => {
    try {
      await cartApi.removeFromCart(cartItemId)
      
      // 从本地缓存中移除
      cartItems.value = cartItems.value.filter(item => item.cartItemId !== cartItemId)
      lastUpdateTime.value = Date.now()
    } catch (error) {
      console.error('删除购物车商品失败:', error)
      throw error
    }
  }

  // 批量删除商品
  const removeMultipleItems = async (cartItemIds: number[]) => {
    try {
      const deletePromises = cartItemIds.map(id => cartApi.removeFromCart(id))
      await Promise.all(deletePromises)
      
      // 从本地缓存中移除
      cartItems.value = cartItems.value.filter(
        item => !cartItemIds.includes(item.cartItemId)
      )
      lastUpdateTime.value = Date.now()
    } catch (error) {
      console.error('批量删除失败:', error)
      throw error
    }
  }

  // 清空购物车
  const clearCart = () => {
    cartItems.value = []
    lastUpdateTime.value = Date.now()
  }

  // 用户登出时清空购物车缓存
  const onUserLogout = () => {
    cartItems.value = []
    lastUpdateTime.value = 0
  }

  // 用户登录时刷新购物车
  const onUserLogin = async () => {
    await loadCartItems(true)
  }

  // 获取指定商品的购物车项
  const getCartItem = (productId: number) => {
    return cartItems.value.find(item => item.product.productId === productId)
  }

  // 检查商品是否在购物车中
  const hasProduct = (productId: number) => {
    return cartItems.value.some(item => item.product.productId === productId)
  }

  // 获取商品在购物车中的数量
  const getProductQuantity = (productId: number) => {
    const item = getCartItem(productId)
    return item ? item.quantity : 0
  }

  return {
    // 状态
    cartItems,
    loading,
    
    // 计算属性
    cartCount,
    cartTotal,
    isEmpty,
    
    // 方法
    loadCartItems,
    addToCart,
    updateItemQuantity,
    removeFromCart,
    removeMultipleItems,
    clearCart,
    onUserLogout,
    onUserLogin,
    getCartItem,
    hasProduct,
    getProductQuantity,
    isCacheExpired
  }
}) 