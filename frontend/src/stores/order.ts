import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { orderApi, type OrderStats } from '@/api/order'
import { useUserStore } from './user'

export const useOrderStore = defineStore('order', () => {
  // 状态
  const orderStats = ref<OrderStats>({
    totalOrders: 0,
    pendingOrders: 0,
    paidOrders: 0,
    shippedOrders: 0,
    deliveredOrders: 0,
    cancelledOrders: 0,
    totalAmount: 0
  })
  
  const loading = ref(false)
  const lastUpdateTime = ref<number>(0)
  const cacheExpireTime = 10 * 60 * 1000 // 缓存10分钟过期

  // 计算属性
  const hasOrders = computed(() => orderStats.value.totalOrders > 0)
  const hasPendingOrders = computed(() => orderStats.value.pendingOrders > 0)

  // 检查缓存是否过期
  const isCacheExpired = () => {
    return Date.now() - lastUpdateTime.value > cacheExpireTime
  }

  // 加载订单统计信息
  const loadOrderStats = async (forceRefresh = false) => {
    const userStore = useUserStore()
    
    if (!userStore.isLoggedIn) {
      resetOrderData()
      return
    }

    if (!forceRefresh && !isCacheExpired() && orderStats.value.totalOrders > 0) {
      return orderStats.value
    }

    loading.value = true
    try {
      const response = await orderApi.getOrderStats()
      if (response.data) {
        orderStats.value = response.data
        lastUpdateTime.value = Date.now()
      }
    } catch (error) {
      console.error('加载订单统计失败:', error)
    } finally {
      loading.value = false
    }

    return orderStats.value
  }

  // 重置订单数据
  const resetOrderData = () => {
    orderStats.value = {
      totalOrders: 0,
      pendingOrders: 0,
      paidOrders: 0,
      shippedOrders: 0,
      deliveredOrders: 0,
      cancelledOrders: 0,
      totalAmount: 0
    }
    lastUpdateTime.value = 0
  }

  // 用户登出时清空订单缓存
  const onUserLogout = () => {
    resetOrderData()
  }

  // 用户登录时刷新订单数据
  const onUserLogin = async () => {
    await loadOrderStats(true)
  }

  return {
    // 状态
    orderStats,
    loading,
    
    // 计算属性
    hasOrders,
    hasPendingOrders,
    
    // 方法
    loadOrderStats,
    resetOrderData,
    onUserLogout,
    onUserLogin,
    isCacheExpired
  }
}) 