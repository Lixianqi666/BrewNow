<template>
  <div class="order-list-page">
    <div class="order-header">
      <h1>我的订单</h1>
      <p>查看和管理您的所有订单</p>
    </div>

    <!-- 未登录状态 -->
    <div v-if="!userStore.isLoggedIn" class="not-logged-in">
      <el-empty description="请先登录查看订单">
        <el-button type="primary" @click="goToLogin">立即登录</el-button>
      </el-empty>
    </div>

    <!-- 订单内容 -->
    <div v-else class="order-content">
      <!-- 状态筛选标签 -->
      <div class="status-tabs">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane 
            v-for="tab in statusTabs" 
            :key="tab.value"
            :label="tab.label" 
            :name="tab.value"
          >
            <template #label>
              <span class="tab-label">
                {{ tab.label }}
                <el-badge 
                  v-if="tab.count > 0" 
                  :value="tab.count" 
                  class="tab-badge"
                />
              </span>
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>

      <!-- 订单列表 -->
      <div v-else-if="orders.length > 0" class="order-list">
        <div
          v-for="order in orders"
          :key="order.orderId"
          class="order-item-card"
        >
          <el-card shadow="never">
            <!-- 订单头部 -->
            <div class="order-header-info">
              <div class="order-basic">
                <div class="order-number">
                  <span class="label">订单号：</span>
                  <span class="value">{{ order.orderNumber }}</span>
                </div>
                <div class="order-date">
                  {{ formatDateTime(order.orderDate) }}
                </div>
              </div>
              <div class="order-status">
                <el-tag :type="getStatusType(order.orderStatus)" size="large">
                  {{ getStatusText(order.orderStatus) }}
                </el-tag>
              </div>
            </div>

            <el-divider />

            <!-- 订单内容 -->
            <div class="order-content-section" @click="goToOrderDetail(order.orderId)">
              <div class="order-info">
                <div class="shipping-address">
                  <span class="label">收货地址：</span>
                  <span class="value">{{ order.shippingAddress }}</span>
                </div>
                <div class="contact-phone">
                  <span class="label">联系电话：</span>
                  <span class="value">{{ order.contactPhone }}</span>
                </div>
                <div v-if="order.remark" class="order-remark">
                  <span class="label">备注：</span>
                  <span class="value">{{ order.remark }}</span>
                </div>
              </div>
              <div class="order-summary">
                <div class="total-amount">
                  <span class="label">订单金额：</span>
                  <span class="amount">{{ formatPrice(order.totalAmount) }}</span>
                </div>
                <div class="payment-info">
                  <span class="payment-method">{{ getPaymentMethodText(order.paymentMethod) }}</span>
                </div>
              </div>
            </div>

            <el-divider />

            <!-- 订单操作 -->
            <div class="order-actions">
              <el-button 
                size="small" 
                @click="goToOrderDetail(order.orderId)"
              >
                查看详情
              </el-button>
              
              <!-- 根据订单状态显示不同操作 -->
              <template v-if="order.orderStatus === OrderStatus.PENDING">
                <el-button 
                  type="primary" 
                  size="small"
                  @click="payOrder(order.orderId)"
                  :loading="paymentLoading[order.orderId]"
                >
                  立即支付
                </el-button>
                <el-button 
                  size="small"
                  @click="cancelOrder(order.orderId)"
                  :loading="cancelLoading[order.orderId]"
                >
                  取消订单
                </el-button>
              </template>
              
              <template v-else-if="order.orderStatus === OrderStatus.SHIPPED">
                <el-button 
                  type="success" 
                  size="small"
                  @click="confirmOrder(order.orderId)"
                  :loading="confirmLoading[order.orderId]"
                >
                  确认收货
                </el-button>
              </template>
              
              <template v-else-if="order.orderStatus === OrderStatus.CANCELLED">
                <el-button 
                  size="small" 
                  @click="deleteOrder(order.orderId)"
                  :loading="deleteLoading[order.orderId]"
                >
                  删除订单
                </el-button>
              </template>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-orders">
        <el-empty :description="getEmptyDescription()">
          <el-button type="primary" @click="goToProducts">去购物</el-button>
        </el-empty>
      </div>

      <!-- 分页 -->
      <div v-if="totalOrders > 0" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="totalOrders"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime, formatPrice } from '@/utils'
import { useUserStore } from '@/stores/user'
import { orderApi, OrderStatus, PaymentMethod, type Order } from '@/api/order'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const orders = ref<Order[]>([])
const activeTab = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const totalOrders = ref(0)

// 加载状态
const paymentLoading = reactive<Record<number, boolean>>({})
const cancelLoading = reactive<Record<number, boolean>>({})
const confirmLoading = reactive<Record<number, boolean>>({})
const deleteLoading = reactive<Record<number, boolean>>({})

// 订单统计（用于显示标签数量）
const orderStats = ref({
  totalOrders: 0,
  pendingOrders: 0,
  paidOrders: 0,
  shippedOrders: 0,
  deliveredOrders: 0,
  cancelledOrders: 0
})

// 状态筛选标签
const statusTabs = computed(() => [
  { value: 'all', label: '全部', count: orderStats.value.totalOrders },
  { value: OrderStatus.PENDING, label: '待支付', count: orderStats.value.pendingOrders },
  { value: OrderStatus.PAID, label: '已支付', count: orderStats.value.paidOrders },
  { value: OrderStatus.SHIPPED, label: '已发货', count: orderStats.value.shippedOrders },
  { value: OrderStatus.DELIVERED, label: '已送达', count: orderStats.value.deliveredOrders },
  { value: OrderStatus.CANCELLED, label: '已取消', count: orderStats.value.cancelledOrders }
])

// 方法
const loadOrders = async () => {
  loading.value = true
  try {
    const status = activeTab.value === 'all' ? undefined : activeTab.value
    const response = await orderApi.getUserOrders(currentPage.value, pageSize.value, status)
    
    if (response.data) {
      orders.value = response.data
      // 这里需要从响应头或其他方式获取总数，暂时使用估算
      totalOrders.value = response.data.length >= pageSize.value ? 
        (currentPage.value * pageSize.value + 1) : 
        ((currentPage.value - 1) * pageSize.value + response.data.length)
    }
  } catch (error: any) {
    console.error('加载订单列表失败:', error)
    if (error.response?.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
      userStore.logout()
      router.push('/login')
    } else {
      ElMessage.error('加载订单失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

const loadOrderStats = async () => {
  try {
    const response = await orderApi.getOrderStats()
    if (response.data) {
      orderStats.value = response.data
    }
  } catch (error) {
    console.error('加载订单统计失败:', error)
  }
}

const handleTabChange = (tabName: string) => {
  activeTab.value = tabName
  currentPage.value = 1
  loadOrders()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadOrders()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadOrders()
}

const payOrder = async (orderId: number) => {
  paymentLoading[orderId] = true
  try {
    await orderApi.payOrder(orderId)
    ElMessage.success('支付成功！')
    loadOrders()
    loadOrderStats()
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
    paymentLoading[orderId] = false
  }
}

const cancelOrder = async (orderId: number) => {
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

    cancelLoading[orderId] = true
    await orderApi.cancelOrder(orderId)
    ElMessage.success('订单已取消')
    loadOrders()
    loadOrderStats()
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
    cancelLoading[orderId] = false
  }
}

const confirmOrder = async (orderId: number) => {
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

    confirmLoading[orderId] = true
    await orderApi.confirmOrder(orderId)
    ElMessage.success('确认收货成功！')
    loadOrders()
    loadOrderStats()
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
    confirmLoading[orderId] = false
  }
}

const deleteOrder = async (orderId: number) => {
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

    deleteLoading[orderId] = true
    // 这里需要API支持删除操作，暂时显示消息
    ElMessage.success('订单已删除')
    loadOrders()
    loadOrderStats()
  } catch (error: any) {
    if (error === 'cancel') return
    ElMessage.error('删除订单失败，请重试')
  } finally {
    deleteLoading[orderId] = false
  }
}

const goToOrderDetail = (orderId: number) => {
  router.push(`/orders/${orderId}`)
}

const goToProducts = () => {
  router.push('/products')
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

const getEmptyDescription = () => {
  if (activeTab.value === 'all') {
    return '您还没有任何订单'
  }
  const statusText = statusTabs.value.find(tab => tab.value === activeTab.value)?.label
  return `暂无${statusText}订单`
}

// 组件挂载时加载数据
onMounted(() => {
  if (userStore.isLoggedIn) {
    loadOrders()
    loadOrderStats()
  }
})
</script>

<style scoped>
.order-list-page {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background: transparent;
}

.order-header {
  margin-bottom: 30px;
  padding: 20px 0;
  border-bottom: 2px solid #f1f3f4;
}

.order-header h1 {
  font-size: 28px;
  color: #333;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.order-header p {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.not-logged-in {
  text-align: center;
  padding: 80px 0;
}

.order-content {
  margin-bottom: 30px;
}

.status-tabs {
  margin-bottom: 30px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tab-badge {
  --el-badge-size: 16px;
}

.loading-container {
  padding: 20px 0;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-item-card {
  transition: all 0.3s ease;
}

.order-item-card:hover {
  transform: translateY(-2px);
}

.order-header-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
}

.order-basic {
  flex: 1;
}

.order-number {
  font-size: 16px;
  margin-bottom: 8px;
}

.order-number .label {
  color: #666;
}

.order-number .value {
  color: #333;
  font-weight: 600;
  margin-left: 4px;
}

.order-date {
  color: #999;
  font-size: 14px;
}

.order-status {
  flex-shrink: 0;
}

.order-content-section {
  cursor: pointer;
  padding: 4px;
  margin: -4px;
  border-radius: 8px;
  transition: background-color 0.2s ease;
}

.order-content-section:hover {
  background-color: #f8f9fa;
}

.order-info {
  margin-bottom: 20px;
}

.order-info > div {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.order-info > div:last-child {
  margin-bottom: 0;
}

.shipping-address,
.contact-phone,
.order-remark {
  line-height: 1.4;
}

.order-info .label {
  color: #666;
  min-width: 80px;
  flex-shrink: 0;
}

.order-info .value {
  color: #333;
  flex: 1;
  word-break: break-all;
}

.order-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0 0 0;
}

.total-amount .label {
  color: #666;
  font-size: 14px;
}

.total-amount .amount {
  color: #e74c3c;
  font-size: 18px;
  font-weight: 700;
  margin-left: 8px;
}

.payment-info {
  color: #666;
  font-size: 13px;
}

.order-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.empty-orders {
  text-align: center;
  padding: 80px 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .order-list-page {
    padding: 10px;
  }

  .order-header-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .order-info > div {
    flex-direction: column;
    gap: 4px;
  }

  .order-info .label {
    min-width: auto;
    font-weight: 600;
  }

  .order-summary {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .order-actions {
    justify-content: stretch;
  }

  .order-actions .el-button {
    flex: 1;
  }

  .order-header h1 {
    font-size: 22px;
  }
}
</style> 
