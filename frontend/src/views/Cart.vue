<template>
  <div class="cart-page">
    <div class="cart-header">
      <h1>购物车</h1>
      <p v-if="cartItems.length > 0">您的购物车中有 {{ cartItems.length }} 件商品</p>
    </div>

    <!-- 未登录状态 -->
    <div v-if="!userStore.isLoggedIn" class="not-logged-in">
      <el-empty description="请先登录查看购物车">
        <el-button type="primary" @click="goToLogin">立即登录</el-button>
      </el-empty>
    </div>

    <!-- 加载状态 -->
    <div v-else-if="loading" class="loading-state">
      <el-skeleton animated>
        <template #template>
          <div v-for="i in 3" :key="i" style="margin-bottom: 20px;">
            <el-skeleton-item variant="rect" style="width: 100%; height: 120px; border-radius: 8px;" />
          </div>
        </template>
      </el-skeleton>
    </div>

    <!-- 空购物车状态 -->
    <div v-else-if="cartItems.length === 0" class="empty-cart">
      <el-empty description="购物车空空如也">
        <el-button type="primary" @click="goToProducts">去购物</el-button>
      </el-empty>
    </div>

    <!-- 购物车商品列表 -->
    <div v-else class="cart-content">
      <div class="cart-items">
        <!-- 全选控制 -->
        <div class="select-all-section">
          <el-checkbox
            v-model="selectAll"
            @change="handleSelectAll"
            :indeterminate="isIndeterminate"
          >
            全选
          </el-checkbox>
          <span class="selected-count">
            已选择 {{ selectedItems.length }} 件商品
          </span>
          <el-button
            type="danger"
            text
            @click="removeSelectedItems"
            :disabled="selectedItems.length === 0"
            :loading="removeLoading"
          >
            删除选中
          </el-button>
        </div>

        <!-- 商品列表 -->
        <div class="cart-item-list">
          <el-card
            v-for="item in cartItems"
            :key="item.cartItemId"
            class="cart-item-card"
            shadow="hover"
          >
            <div class="cart-item">
              <div class="item-select">
                <el-checkbox
                  :model-value="selectedItems.includes(item.cartItemId)"
                  @change="(checked: boolean) => handleItemSelect(item.cartItemId, checked)"
                />
              </div>

              <div class="item-image">
                <img
                  :src="resolveProductImageUrl(item.product.productName, item.product.imageUrl)"
                  :alt="item.product.productName"
                  @error="handleImageError"
                />
              </div>

              <div class="item-info">
                <h3 class="item-name">{{ item.product.productName }}</h3>
                <p class="item-description">{{ item.product.brand }} · {{ item.product.category }}</p>
                <div class="item-stock" v-if="item.product.stockQuantity !== undefined">
                  <span :class="{ 'low-stock': item.product.stockQuantity < 10 }">
                    库存：{{ item.product.stockQuantity }} 件
                  </span>
                </div>
              </div>

              <div class="item-price">
                <span class="current-price">{{ formatPrice(item.product.price) }}</span>
              </div>

              <div class="item-quantity">
                <el-input-number
                  :model-value="item.quantity"
                  :min="1"
                  :max="item.product.stockQuantity || 99"
                  @change="(value: number) => updateQuantity(item.cartItemId, value)"
                  size="small"
                  :loading="quantityLoading[item.cartItemId]"
                />
              </div>

              <div class="item-subtotal">
                <span class="subtotal-price">
                  {{ formatPrice(item.product.price * item.quantity) }}
                </span>
              </div>

              <div class="item-actions">
                <el-button
                  type="danger"
                  text
                  @click="removeItem(item.cartItemId)"
                  :icon="Delete"
                  size="small"
                  :loading="removeLoading"
                >
                  删除
                </el-button>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 购物车统计和结算 -->
      <div class="cart-summary">
        <el-card class="summary-card" shadow="always">
          <div class="summary-header">
            <h3>订单统计</h3>
          </div>

          <div class="summary-details">
            <div class="summary-row">
              <span>商品总数：</span>
              <span>{{ selectedTotalQuantity }} 件</span>
            </div>
            <div class="summary-row">
              <span>商品金额：</span>
              <span>{{ formatPrice(selectedTotalPrice) }}</span>
            </div>
            <div class="summary-row">
              <span>运费：</span>
              <span class="shipping-fee">
                {{ shippingFee > 0 ? formatPrice(shippingFee) : '免运费' }}
              </span>
            </div>
            <el-divider />
            <div class="summary-row total-row">
              <span>应付总额：</span>
              <span class="total-price">{{ formatPrice(finalTotalPrice) }}</span>
            </div>
          </div>

          <div class="summary-actions">
            <el-button
              type="primary"
              size="large"
              @click="proceedToCheckout"
              :disabled="selectedItems.length === 0"
              :loading="checkoutLoading"
              block
            >
              去结算 ({{ selectedItems.length }})
            </el-button>
            <el-button
              size="large"
              @click="continueShopping"
              block
            >
              继续购物
            </el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { formatPrice } from '@/utils'
import type { CartItem } from '@/api/cart'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { resolveProductImageUrl } from '@/utils/productImageResolver'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

// 响应式数据
const cartItems = ref<CartItem[]>([])
const selectedItems = ref<number[]>([])
const loading = ref(false)
const checkoutLoading = ref(false)
const removeLoading = ref(false)
const quantityLoading = reactive<Record<number, boolean>>({})

// 计算属性
const selectAll = computed({
  get: () => selectedItems.value.length === cartItems.value.length && cartItems.value.length > 0,
  set: (value: boolean) => {
    if (value) {
      selectedItems.value = cartItems.value.map(item => item.cartItemId)
    } else {
      selectedItems.value = []
    }
  }
})

const isIndeterminate = computed(() => {
  return selectedItems.value.length > 0 && selectedItems.value.length < cartItems.value.length
})

const selectedTotalQuantity = computed(() => {
  return cartItems.value
    .filter(item => selectedItems.value.includes(item.cartItemId))
    .reduce((total, item) => total + item.quantity, 0)
})

const selectedTotalPrice = computed(() => {
  return cartItems.value
    .filter(item => selectedItems.value.includes(item.cartItemId))
    .reduce((total, item) => total + (item.product.price * item.quantity), 0)
})

const shippingFee = computed(() => {
  // 满99免运费
  return selectedTotalPrice.value >= 99 ? 0 : 10
})

const finalTotalPrice = computed(() => {
  return selectedTotalPrice.value + shippingFee.value
})

// 方法
const loadCartItems = async () => {
  if (!userStore.isLoggedIn) {
    cartItems.value = []
    selectedItems.value = []
    return
  }

  loading.value = true
  try {
    await cartStore.loadCartItems(true)
    cartItems.value = [...cartStore.cartItems]
    // 默认全选
    selectedItems.value = cartItems.value.map(item => item.cartItemId)
  } catch (error: any) {
    console.error('加载购物车失败:', error)
    if (error.response?.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
      userStore.logout()
      router.push('/login')
    } else {
      ElMessage.error('加载购物车失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

const handleSelectAll = (checked: boolean) => {
  if (checked) {
    selectedItems.value = cartItems.value.map(item => item.cartItemId)
  } else {
    selectedItems.value = []
  }
}

const handleItemSelect = (cartItemId: number, checked: boolean) => {
  if (checked) {
    selectedItems.value.push(cartItemId)
  } else {
    selectedItems.value = selectedItems.value.filter(id => id !== cartItemId)
  }
}

const updateQuantity = async (cartItemId: number, quantity: number) => {
  if (quantity < 1) return

  quantityLoading[cartItemId] = true
  try {
    await cartStore.updateItemQuantity(cartItemId, quantity)

    // 更新本地数据
    const item = cartItems.value.find(item => item.cartItemId === cartItemId)
    if (item) {
      item.quantity = quantity
    }

    ElMessage.success('数量已更新')
  } catch (error: any) {
    console.error('更新数量失败:', error)
    if (error.response?.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
      userStore.logout()
      router.push('/login')
    } else {
      ElMessage.error('更新数量失败，请重试')
    }
  } finally {
    quantityLoading[cartItemId] = false
  }
}

const removeItem = async (cartItemId: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要从购物车中删除这件商品吗？',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    removeLoading.value = true

    await cartStore.removeFromCart(cartItemId)

    // 更新本地数据
    cartItems.value = [...cartStore.cartItems]
    selectedItems.value = selectedItems.value.filter(id => id !== cartItemId)

    ElMessage.success('商品已删除')

  } catch (error: any) {
    if (error === 'cancel') {
      // 用户取消删除
      return
    }

    console.error('删除商品失败:', error)
    if (error.response?.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
      userStore.logout()
      router.push('/login')
    } else {
      ElMessage.error('删除商品失败，请重试')
    }
  } finally {
    removeLoading.value = false
  }
}

const removeSelectedItems = async () => {
  if (selectedItems.value.length === 0) return

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedItems.value.length} 件商品吗？`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    removeLoading.value = true
    const removingIds = [...selectedItems.value]

    await cartStore.removeMultipleItems(removingIds)

    // 更新本地数据
    cartItems.value = [...cartStore.cartItems]
    selectedItems.value = []

    ElMessage.success('选中商品已删除')

  } catch (error: any) {
    if (error === 'cancel') {
      // 用户取消删除
      return
    }

    console.error('批量删除失败:', error)
    if (error.response?.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
      userStore.logout()
      router.push('/login')
    } else {
      ElMessage.error('删除商品失败，请重试')
    }
  } finally {
    removeLoading.value = false
  }
}

const proceedToCheckout = async () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }

  checkoutLoading.value = true
  try {
    // 跳转到订单确认页面，传递选中的商品信息
    const selectedCartItems = cartItems.value.filter(item =>
      selectedItems.value.includes(item.cartItemId)
    )

    // 将选中的商品信息存储到sessionStorage，供结算页面使用
    sessionStorage.setItem('checkoutItems', JSON.stringify(selectedCartItems))

    router.push('/checkout')

  } catch {
    ElMessage.error('结算失败，请重试')
  } finally {
    checkoutLoading.value = false
  }
}

const continueShopping = () => {
  router.push('/products')
}

const goToProducts = () => {
  router.push('/products')
}

const goToLogin = () => {
  router.push('/login')
}

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  // 防止死循环：如果已经是占位符了就不再处理
  if (img.src.includes('data:image')) return

  // 使用base64编码的灰色占位符图片
  img.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZjVmNWY1Ii8+CiAgPHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtc2l6ZT0iMTRweCIgZmlsbD0iIzk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPuaaguaXoOWbvueJhzwvdGV4dD4KPC9zdmc+'
}

// 组件挂载时加载数据
onMounted(() => {
  if (userStore.isLoggedIn) {
    loadCartItems()
  }
})
</script>

<style scoped>
.cart-page {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: linear-gradient(180deg, #f7f3eb 0%, #f3ece0 100%);
  border-radius: 24px;
}

.cart-header {
  margin-bottom: 30px;
  padding: 20px 0;
  border-bottom: 2px solid rgba(200, 155, 82, 0.22);
}

.cart-header h1 {
  font-size: 32px;
  color: #173a2f;
  margin: 0 0 12px 0;
  font-weight: 700;
}

.cart-header p {
  color: #6f6759;
  margin: 0;
  font-size: 16px;
}

.not-logged-in,
.empty-cart {
  text-align: center;
  padding: 80px 0;
}

.loading-state {
  padding: 20px 0;
}

.cart-content {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

.cart-items {
  flex: 1;
  min-width: 0;
}

.select-all-section {
  background: rgba(255, 255, 255, 0.84);
  backdrop-filter: blur(8px);
  padding: 20px 24px;
  border-radius: 18px;
  box-shadow: 0 14px 28px rgba(23, 58, 47, 0.1);
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 24px;
  border: 1px solid rgba(200, 155, 82, 0.16);
}

.selected-count {
  color: #6f6759;
  font-size: 14px;
}

.cart-item-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cart-item-card {
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid rgba(200, 155, 82, 0.16);
  background: linear-gradient(180deg, #fff, #faf5eb);
  transition: all 0.3s ease;
}

.cart-item-card:hover {
  border-color: rgba(46, 98, 79, 0.32);
  box-shadow: 0 16px 30px rgba(23, 58, 47, 0.12);
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
}

.item-select {
  flex-shrink: 0;
}

.item-image {
  flex-shrink: 0;
  width: 100px;
  height: 100px;
  border-radius: 14px;
  overflow: hidden;
  background:
    radial-gradient(circle at top right, rgba(200, 155, 82, 0.25), transparent 35%),
    linear-gradient(135deg, #f3e6d2, #ddd0be);
  border: 1px solid rgba(200, 155, 82, 0.18);
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  min-width: 250px;
  padding-right: 20px;
}

.item-name {
  font-size: 16px;
  font-weight: 600;
  color: #173a2f;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.item-description {
  font-size: 14px;
  color: #7a7468;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.item-stock {
  font-size: 12px;
  color: #27ae60;
}

.item-stock .low-stock {
  color: #e74c3c;
}

.item-price {
  flex-shrink: 0;
  text-align: center;
  min-width: 100px;
  padding: 0 10px;
}

.current-price {
  font-size: 22px;
  font-weight: 600;
  color: #b97b25;
  display: block;
}

.item-quantity {
  flex-shrink: 0;
  padding: 0 10px;
}

.item-subtotal {
  flex-shrink: 0;
  text-align: center;
  min-width: 120px;
  padding: 0 10px;
}

.subtotal-price {
  font-size: 24px;
  font-weight: 700;
  color: #b97b25;
}

.item-actions {
  flex-shrink: 0;
  min-width: 60px;
}

.cart-summary {
  width: 380px;
  flex-shrink: 0;
}

.summary-card {
  border-radius: 20px;
  position: sticky;
  top: 20px;
  border: 1px solid rgba(200, 155, 82, 0.16);
  background: linear-gradient(180deg, #fffdf7 0%, #f6efe2 100%);
  box-shadow: 0 16px 34px rgba(23, 58, 47, 0.1);
}

.summary-header h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  color: #173a2f;
}

.summary-details {
  margin-bottom: 20px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
}

.summary-row span:first-child {
  color: #6f6759;
}

.summary-row span:last-child {
  color: #4b473f;
  font-weight: 500;
}

.shipping-fee {
  color: #27ae60 !important;
}

.total-row {
  font-size: 16px;
  font-weight: 600;
}

.total-row span:last-child {
  color: #b97b25;
  font-size: 24px;
}

.summary-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-actions .el-button {
  height: 52px;
  border-radius: 16px;
  margin: 0 !important;
  font-size: 18px;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cart-page {
    padding: 10px;
  }

  .cart-content {
    flex-direction: column;
    gap: 20px;
  }

  .cart-summary {
    width: 100%;
    order: -1;
  }

  .cart-item {
    flex-wrap: wrap;
    gap: 16px;
    padding: 16px;
  }

  .item-image {
    width: 80px;
    height: 80px;
  }

  .item-info {
    min-width: auto;
    flex-basis: 100%;
    padding-right: 0;
  }

  .item-price,
  .item-quantity,
  .item-subtotal {
    min-width: auto;
    padding: 0;
    text-align: left;
  }

  .select-all-section {
    flex-wrap: wrap;
    gap: 16px;
    padding: 16px 20px;
  }

  .cart-header h1 {
    font-size: 24px;
  }
}
</style>
