<template>
  <div class="order-detail-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item @click="goToOrders">我的订单</el-breadcrumb-item>
        <el-breadcrumb-item>订单详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 未登录状态 -->
    <div v-if="!userStore.isLoggedIn" class="not-logged-in">
      <el-empty description="请先登录查看订单详情">
        <BaseButton type="primary" @click="goToLogin">立即登录</BaseButton>
      </el-empty>
    </div>

    <!-- 加载状态 -->
    <div v-else-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 订单详情内容 -->
    <div v-else-if="orderDetail" class="order-detail-content">
      <!-- 订单状态和基本信息 -->
      <el-card class="order-status-card" shadow="hover">
        <div class="order-status-header">
          <div class="status-info">
            <h1>订单详情</h1>
            <div class="order-meta">
              <span class="order-number">订单号：{{ orderDetail.order.orderNumber }}</span>
              <span class="order-date">下单时间：{{ formatDateTime(orderDetail.order.orderDate) }}</span>
            </div>
          </div>
          <div class="status-badge">
            <el-tag :type="getStatusType(orderDetail.order.orderStatus)" size="large">
              {{ getStatusText(orderDetail.order.orderStatus) }}
            </el-tag>
          </div>
        </div>

        <!-- 订单进度条 -->
        <div class="order-progress">
          <el-steps :active="getProgressStep(orderDetail.order.orderStatus)" align-center>
            <el-step title="订单创建" description="订单已提交" />
            <el-step title="支付完成" description="支付已确认" />
            <el-step title="商品发货" description="商家已发货" />
            <el-step title="订单完成" description="确认收货" />
          </el-steps>
        </div>
      </el-card>

      <!-- 收货信息 -->
      <el-card class="shipping-info-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><Location /></el-icon>
            <span>收货信息</span>
          </div>
        </template>
        <div class="shipping-info">
          <div class="info-row">
            <span class="label">收货地址：</span>
            <span class="value">{{ orderDetail.order.shippingAddress }}</span>
          </div>
          <div class="info-row">
            <span class="label">联系电话：</span>
            <span class="value">{{ orderDetail.order.contactPhone }}</span>
          </div>
          <div v-if="orderDetail.order.remark" class="info-row">
            <span class="label">订单备注：</span>
            <span class="value">{{ orderDetail.order.remark }}</span>
          </div>
        </div>
      </el-card>

      <!-- 商品信息 -->
      <el-card class="products-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><ShoppingBag /></el-icon>
            <span>商品信息</span>
          </div>
        </template>
        <div class="products-list">
          <div
            v-for="item in orderDetail.orderItems"
            :key="item.orderItemId"
            class="product-item"
          >
            <div class="product-image">
              <img
                :src="getProductImageUrl(item)"
                :alt="item.productNameSnapshot || `商品${item.productId}`"
                @error="handleImageError"
              />
            </div>
            <div class="product-info">
              <h3>{{ item.productNameSnapshot || `茶叶商品 #${item.productId}` }}</h3>
              <p class="product-meta">商品编号：{{ item.productId }}</p>
              <div class="price-info">
                <span class="unit-price">单价：{{ formatPrice(item.unitPrice) }}</span>
                <span class="quantity">数量：{{ item.quantity }}</span>
              </div>
              <div v-if="canShowReviewEntry(orderDetail.order.orderStatus)" class="review-entry">
                <BaseButton
                  v-if="!item.reviewed"
                  type="primary"
                  plain
                  size="small"
                  @click="openReviewDialog(item)"
                >
                  去评价
                </BaseButton>
                <el-tag v-else type="success" effect="plain">已评价</el-tag>
              </div>
            </div>
            <div class="product-total">
              <span class="subtotal">小计：{{ formatPrice(item.subtotal) }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 订单金额 -->
      <el-card class="amount-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><Money /></el-icon>
            <span>订单金额</span>
          </div>
        </template>
        <div class="amount-info">
          <div class="amount-row">
            <span class="label">商品总额：</span>
            <span class="value">{{ formatPrice(getSubtotal()) }}</span>
          </div>
          <div class="amount-row">
            <span class="label">运费：</span>
            <span class="value shipping-fee">{{ getShippingFee() > 0 ? formatPrice(getShippingFee()) : '免运费' }}</span>
          </div>
          <el-divider />
          <div class="amount-row total-row">
            <span class="label">实付金额：</span>
            <span class="value total-amount">{{ formatPrice(orderDetail.order.totalAmount) }}</span>
          </div>
          <div class="payment-method">
            <span class="label">支付方式：</span>
            <span class="value">{{ getPaymentMethodText(orderDetail.order.paymentMethod) }}</span>
          </div>
        </div>
      </el-card>

      <!-- 订单操作 -->
      <div class="order-actions">
        <el-card shadow="hover">
          <div class="actions-content">
            <div class="actions-left">
              <BaseButton @click="goToOrders">返回订单列表</BaseButton>
            </div>
            <div class="actions-right">
              <!-- 根据订单状态显示不同操作 -->
              <template v-if="orderDetail.order.orderStatus === OrderStatus.PENDING">
                <BaseButton 
                  type="primary"
                  @click="payOrder"
                  :loading="paymentLoading"
                >
                  立即支付
                </BaseButton>
                <BaseButton 
                  @click="cancelOrder"
                  :loading="cancelLoading"
                >
                  取消订单
                </BaseButton>
              </template>
              
              <template v-else-if="orderDetail.order.orderStatus === OrderStatus.SHIPPED">
                <BaseButton 
                  type="success"
                  @click="confirmOrder"
                  :loading="confirmLoading"
                >
                  确认收货
                </BaseButton>
              </template>
              
              <template v-else-if="orderDetail.order.orderStatus === OrderStatus.CANCELLED">
                <BaseButton 
                  @click="deleteOrder"
                  :loading="deleteLoading"
                >
                  删除订单
                </BaseButton>
              </template>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 订单不存在 -->
    <div v-else class="order-not-found">
      <el-empty description="订单不存在或已被删除">
        <BaseButton type="primary" @click="goToOrders">返回订单列表</BaseButton>
      </el-empty>
    </div>
  </div>

  <el-dialog v-model="reviewDialogVisible" title="商品评价" width="520px">
    <el-form v-if="reviewForm.orderItemId" label-width="88px">
      <el-form-item label="评分">
        <el-rate v-model="reviewForm.rating" />
      </el-form-item>
      <el-form-item label="评价内容">
        <el-input
          v-model="reviewForm.content"
          type="textarea"
          :rows="4"
          maxlength="300"
          show-word-limit
          placeholder="说说这款茶的香气、口感和整体体验"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <BaseButton @click="reviewDialogVisible = false">取消</BaseButton>
      <BaseButton type="primary" :loading="reviewSubmitting" @click="submitReview">提交评价</BaseButton>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Location, 
  ShoppingBag, 
  Money 
} from '@element-plus/icons-vue'
import { formatDateTime, formatPrice } from '@/utils'
import { getDefaultProductImage, resolveProductImageUrl } from '@/utils/productImageResolver'
import { useUserStore } from '@/stores/user'
import { orderApi, OrderStatus, PaymentMethod, type OrderDetail } from '@/api/order'
import { reviewApi } from '@/api/review'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const orderDetail = ref<OrderDetail | null>(null)
const paymentLoading = ref(false)
const cancelLoading = ref(false)
const confirmLoading = ref(false)
const deleteLoading = ref(false)
const defaultProductImage = getDefaultProductImage()
const reviewDialogVisible = ref(false)
const reviewSubmitting = ref(false)
const reviewForm = ref({
  orderItemId: 0,
  rating: 5,
  content: ''
})

// 计算属性
const orderId = computed(() => {
  return parseInt(route.params.id as string)
})

// 方法
const loadOrderDetail = async () => {
  if (!orderId.value || isNaN(orderId.value)) {
    ElMessage.error('订单ID无效')
    router.push('/orders')
    return
  }

  loading.value = true
  try {
    const response = await orderApi.getOrderDetail(orderId.value)
    if (response.data) {
      orderDetail.value = response.data
    } else {
      ElMessage.error('订单不存在')
      router.push('/orders')
    }
  } catch (error: any) {
    console.error('加载订单详情失败:', error)
    if (error.response?.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
      userStore.logout()
      router.push('/login')
    } else if (error.response?.status === 404) {
      ElMessage.error('订单不存在')
      router.push('/orders')
    } else {
      ElMessage.error('加载订单详情失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

const payOrder = async () => {
  if (!orderDetail.value) return

  paymentLoading.value = true
  try {
    await orderApi.payOrder(orderDetail.value.order.orderId)
    ElMessage.success('支付成功！')
    loadOrderDetail() // 重新加载订单详情
  } catch (error: any) {
    console.error('支付失败:', error)
    if (error.response?.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
      userStore.logout()
      router.push('/login')
    } else {
      ElMessage.error('支付失败，请重试')
    }
  } finally {
    paymentLoading.value = false
  }
}

const cancelOrder = async () => {
  if (!orderDetail.value) return

  try {
    await ElMessageBox.confirm(
      '确定要取消这个订单吗？取消后无法恢复。',
      '取消订单',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '再想想',
        type: 'warning'
      }
    )

    cancelLoading.value = true
    await orderApi.cancelOrder(orderDetail.value.order.orderId)
    ElMessage.success('订单已取消')
    loadOrderDetail()
  } catch (error: any) {
    if (error === 'cancel') return
    
    console.error('取消订单失败:', error)
    if (error.response?.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
      userStore.logout()
      router.push('/login')
    } else {
      ElMessage.error('取消订单失败，请重试')
    }
  } finally {
    cancelLoading.value = false
  }
}

const confirmOrder = async () => {
  if (!orderDetail.value) return

  try {
    await ElMessageBox.confirm(
      '确认已收到货物吗？确认后订单将完成。',
      '确认收货',
      {
        confirmButtonText: '确认收货',
        cancelButtonText: '再想想',
        type: 'info'
      }
    )

    confirmLoading.value = true
    await orderApi.confirmOrder(orderDetail.value.order.orderId)
    ElMessage.success('确认收货成功！')
    loadOrderDetail()
  } catch (error: any) {
    if (error === 'cancel') return
    
    console.error('确认收货失败:', error)
    if (error.response?.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
      userStore.logout()
      router.push('/login')
    } else {
      ElMessage.error('确认收货失败，请重试')
    }
  } finally {
    confirmLoading.value = false
  }
}

const deleteOrder = async () => {
  if (!orderDetail.value) return

  try {
    await ElMessageBox.confirm(
      '确定要删除这个订单吗？删除后无法恢复。',
      '删除订单',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    deleteLoading.value = true
    await orderApi.deleteOrder(orderDetail.value.order.orderId)
    ElMessage.success('订单已删除')
    router.push('/orders')
  } catch (error: any) {
    if (error === 'cancel') return
    ElMessage.error('删除订单失败，请重试')
  } finally {
    deleteLoading.value = false
  }
}

const goToOrders = () => {
  router.push('/orders')
}

const goToLogin = () => {
  router.push('/login')
}

const getStatusType = (status: OrderStatus) => {
  const statusMap = {
    [OrderStatus.PENDING]: 'warning',
    [OrderStatus.PAID]: 'info',
    [OrderStatus.SHIPPED]: 'primary',
    [OrderStatus.DELIVERED]: 'success',
    [OrderStatus.CANCELLED]: 'danger',
    [OrderStatus.REFUNDED]: 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status: OrderStatus) => {
  const statusMap = {
    [OrderStatus.PENDING]: '待支付',
    [OrderStatus.PAID]: '已支付',
    [OrderStatus.SHIPPED]: '已发货',
    [OrderStatus.DELIVERED]: '已送达',
    [OrderStatus.CANCELLED]: '已取消',
    [OrderStatus.REFUNDED]: '已退款'
  }
  return statusMap[status] || status
}

const getPaymentMethodText = (method: PaymentMethod) => {
  const methodMap = {
    [PaymentMethod.ALIPAY]: '支付宝',
    [PaymentMethod.WECHAT]: '微信支付',
    [PaymentMethod.CREDIT_CARD]: '信用卡',
    [PaymentMethod.CASH]: '货到付款'
  }
  return methodMap[method] || method
}

const getProgressStep = (status: OrderStatus) => {
  const stepMap = {
    [OrderStatus.PENDING]: 0,
    [OrderStatus.PAID]: 1,
    [OrderStatus.SHIPPED]: 2,
    [OrderStatus.DELIVERED]: 3,
    [OrderStatus.CANCELLED]: 0,
    [OrderStatus.REFUNDED]: 0
  }
  return stepMap[status] || 0
}

const getProductImageUrl = (item: OrderDetail['orderItems'][number]) => {
  return resolveProductImageUrl(item.productNameSnapshot, item.imageUrlSnapshot) || defaultProductImage
}

const canShowReviewEntry = (status: OrderStatus) => {
  return status === OrderStatus.SHIPPED || status === OrderStatus.DELIVERED
}

const openReviewDialog = (item: OrderDetail['orderItems'][number]) => {
  reviewForm.value = {
    orderItemId: item.orderItemId,
    rating: 5,
    content: ''
  }
  reviewDialogVisible.value = true
}

const submitReview = async () => {
  if (!reviewForm.value.orderItemId) return
  reviewSubmitting.value = true
  try {
    await reviewApi.submitReview({
      orderItemId: reviewForm.value.orderItemId,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content
    })
    ElMessage.success('评价提交成功')
    reviewDialogVisible.value = false
    await loadOrderDetail()
  } catch (error) {
    console.error('提交评价失败:', error)
  } finally {
    reviewSubmitting.value = false
  }
}

const getSubtotal = () => {
  if (!orderDetail.value) return 0
  return orderDetail.value.orderItems.reduce((total, item) => total + item.subtotal, 0)
}

const getShippingFee = () => {
  if (!orderDetail.value) return 0
  const subtotal = getSubtotal()
  return orderDetail.value.order.totalAmount - subtotal
}

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  if (img.src.includes('data:image')) return
  
  img.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZjVmNWY1Ii8+CiAgPHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtc2l6ZT0iMTRweCIgZmlsbD0iIzk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPuaaguaXoOWbvueJhzwvdGV4dD4KPC9zdmc+'
}

// 组件挂载时加载数据
onMounted(() => {
  if (userStore.isLoggedIn) {
    loadOrderDetail()
  }
})
</script>

<style scoped>
.order-detail-page {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.breadcrumb {
  margin-bottom: 20px;
}

.breadcrumb .el-breadcrumb-item {
  cursor: pointer;
}

.not-logged-in,
.order-not-found {
  text-align: center;
  padding: 80px 0;
}

.loading-container {
  padding: 20px 0;
}

.order-detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.order-status-card {
  border-radius: 12px;
}

.order-status-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
}

.status-info h1 {
  margin: 0 0 12px 0;
  font-size: 24px;
  color: #333;
  font-weight: 600;
}

.order-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.order-meta span {
  color: #666;
  font-size: 14px;
}

.order-number {
  font-weight: 500;
}

.status-badge {
  flex-shrink: 0;
}

.order-progress {
  margin-top: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #333;
}

.shipping-info,
.amount-info {
  padding: 4px 0;
}

.info-row,
.amount-row {
  display: flex;
  margin-bottom: 12px;
  font-size: 14px;
}

.info-row:last-child,
.amount-row:last-child {
  margin-bottom: 0;
}

.info-row .label,
.amount-row .label {
  color: #666;
  min-width: 80px;
  flex-shrink: 0;
}

.info-row .value,
.amount-row .value {
  color: #333;
  flex: 1;
}

.amount-row.total-row {
  font-size: 16px;
  font-weight: 600;
}

.total-amount {
  color: #e74c3c !important;
  font-size: 18px;
}

.shipping-fee {
  color: #27ae60 !important;
}

.payment-method {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f1f3f4;
  display: flex;
  font-size: 14px;
}

.payment-method .label {
  color: #666;
  min-width: 80px;
}

.payment-method .value {
  color: #333;
}

.products-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #f1f3f4;
}

.product-item:last-child {
  border-bottom: none;
}

.product-image {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
  border: 1px solid #e0e0e0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-info h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.product-meta {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: #999;
}

.price-info {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #666;
}

.review-entry {
  margin-top: 12px;
}


.product-total {
  flex-shrink: 0;
  text-align: right;
}

.subtotal {
  font-size: 16px;
  font-weight: 600;
  color: #e74c3c;
}

.order-actions .actions-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.actions-right {
  display: flex;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .order-detail-page {
    padding: 10px;
  }

  .order-status-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .status-info h1 {
    font-size: 20px;
  }

  .order-meta {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 16px;
  }

  .product-item {
    flex-wrap: wrap;
    gap: 12px;
  }

  .product-image {
    width: 60px;
    height: 60px;
  }

  .product-info {
    flex-basis: 100%;
    order: 1;
  }

  .product-total {
    flex-basis: 100%;
    text-align: left;
    order: 2;
  }

  .price-info {
    flex-direction: column;
    gap: 4px;
  }

  .actions-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .actions-right {
    justify-content: stretch;
  }

  .actions-right .el-button {
    flex: 1;
  }
}
</style> 
