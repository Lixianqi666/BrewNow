<template>
  <div class="merchant-orders">
    <el-card class="page-container">
      <template #header>
        <div class="card-header">
          <div>
            <h2>订单管理</h2>
          </div>
          <div class="header-actions">
            <el-select v-model="statusFilter" class="status-select" @change="loadOrders">
              <el-option label="全部状态" value="ALL" />
              <el-option label="待支付" value="PENDING" />
              <el-option label="已支付" value="PAID" />
              <el-option label="已发货" value="SHIPPED" />
              <el-option label="已送达" value="DELIVERED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
            <div class="refresh-info" v-if="statusFilter !== 'ALL'">
              当前: {{ getStatusLabel(statusFilter) }}
            </div>
            <BaseButton class="refresh-button" :loading="loading" @click="loadOrders">刷新</BaseButton>
          </div>
        </div>
      </template>

      <div class="content">
        <div v-if="orders.length" class="table-shell">
          <el-table :data="orders" border stripe class="order-table brew-table" table-layout="fixed">
            <el-table-column prop="orderId" label="订单ID" width="96" align="center" fixed="left" />
            <el-table-column prop="orderNumber" label="订单号" width="220" show-overflow-tooltip />
            <el-table-column prop="userId" label="用户ID" width="96" align="center" />
            <el-table-column prop="productNames" label="包含商品" width="280" show-overflow-tooltip />
            <el-table-column prop="itemCount" label="件数" width="90" align="center" />
            <el-table-column label="金额" width="130" align="center">
              <template #default="{ row }">
                <span class="amount-text">￥{{ Number(row.totalAmount || 0).toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="支付方式" width="120" align="center">
              <template #default="{ row }">
                <span>{{ getPaymentMethodLabel(row.paymentMethod) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.orderStatus)" effect="light" round>
                  {{ getStatusLabel(row.orderStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="orderDate" label="下单时间" width="190" show-overflow-tooltip />
            <el-table-column label="操作" width="240" fixed="right" align="center">
              <template #default="{ row }">
                <div class="action-group">
                  <BaseButton class="detail-button" size="small" @click="showOrderDetail(row)">查看详情</BaseButton>
                  <BaseButton
                    v-if="row.orderStatus === 'PAID'"
                    class="ship-button"
                    type="warning"
                    size="small"
                    :loading="shippingOrderId === row.orderId"
                    @click="shipOrder(row)"
                  >
                    标记发货
                  </BaseButton>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else description="暂无订单数据" />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="订单详情" width="620px">
      <div v-if="currentOrder" class="detail-grid">
        <div><strong>订单号：</strong>{{ currentOrder.orderNumber }}</div>
        <div><strong>用户ID：</strong>{{ currentOrder.userId }}</div>
        <div><strong>订单状态：</strong>{{ currentOrder.orderStatus }}</div>
        <div><strong>支付方式：</strong>{{ currentOrder.paymentMethod }}</div>
        <div><strong>订单金额：</strong>￥{{ Number(currentOrder.totalAmount || 0).toFixed(2) }}</div>
        <div><strong>商品件数：</strong>{{ currentOrder.itemCount }}</div>
        <div class="full-row"><strong>包含商品：</strong>{{ currentOrder.productNames }}</div>
        <div class="full-row"><strong>收货地址：</strong>{{ currentOrder.shippingAddress || '未提供' }}</div>
        <div><strong>联系电话：</strong>{{ currentOrder.contactPhone || '未提供' }}</div>
        <div><strong>下单时间：</strong>{{ currentOrder.orderDate }}</div>
        <div class="full-row"><strong>订单备注：</strong>{{ currentOrder.remark || '无' }}</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { merchantApi, type MerchantOrderSummary, type MerchantOrderDetail } from '@/api/merchant'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const orders = ref<MerchantOrderSummary[]>([])
const statusFilter = ref<string>('ALL')
const detailVisible = ref(false)
const currentOrder = ref<MerchantOrderDetail | null>(null)
const shippingOrderId = ref<number | null>(null)

const getStatusLabel = (status?: string) => {
  const labelMap: Record<string, string> = {
    PENDING: '待支付',
    PAID: '已支付',
    SHIPPED: '已发货',
    DELIVERED: '已送达',
    CANCELLED: '已取消'
  }
  return labelMap[(status || '').toUpperCase()] || '未知状态'
}

const getPaymentMethodLabel = (paymentMethod?: string) => {
  const labelMap: Record<string, string> = {
    ALIPAY: '支付宝',
    WECHAT: '微信支付',
    CREDIT_CARD: '银行卡'
  }
  return labelMap[(paymentMethod || '').toUpperCase()] || paymentMethod || '未支付'
}

const getStatusTagType = (status?: string) => {
  const typeMap: Record<string, '' | 'success' | 'warning' | 'danger' | 'info'> = {
    PENDING: 'warning',
    PAID: 'success',
    SHIPPED: '',
    DELIVERED: 'info',
    CANCELLED: 'danger'
  }
  return typeMap[(status || '').toUpperCase()] || 'info'
}

const loadOrders = async () => {
  if (!userStore.isLoggedIn) {
    loading.value = false
    return
  }

  loading.value = true
  try {
    const status = statusFilter.value === 'ALL' ? undefined : statusFilter.value
    const response = await merchantApi.getMerchantOrders(1, 20, status)
    orders.value = response.data?.list || []
  } catch (error) {
    console.error('加载商家订单失败:', error)
    orders.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    loadOrders()
  }
})

const showOrderDetail = async (order: MerchantOrderSummary) => {
  if (!userStore.isLoggedIn) {
    return
  }

  try {
    const response = await merchantApi.getMerchantOrderDetail(order.orderId)
    currentOrder.value = response.data || null
    detailVisible.value = true
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
  }
}

const shipOrder = async (order: MerchantOrderSummary) => {
  if (!userStore.isLoggedIn) {
    return
  }

  try {
    await ElMessageBox.confirm('确认将该订单标记为已发货吗？', '订单发货', {
      confirmButtonText: '确认发货',
      cancelButtonText: '取消',
      type: 'warning'
    })
    shippingOrderId.value = order.orderId
    await merchantApi.shipMerchantOrder(order.orderId)
    ElMessage.success('订单已标记为发货')
    await loadOrders()
    if (detailVisible.value && currentOrder.value?.orderId === order.orderId) {
      await showOrderDetail(order)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('订单发货失败:', error)
      ElMessage.error(error instanceof Error ? error.message : '订单发货失败')
    }
  } finally {
    shippingOrderId.value = null
  }
}
</script>

<style scoped>
.merchant-orders {
  padding: 20px;
}

.page-container {
  max-width: 1440px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.card-header h2 {
  margin: 0;
  color: #303133;
}

.card-header p {
  margin: 6px 0 0;
  color: #6f6252;
  font-size: 13px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-select {
  width: 140px;
}

.refresh-info {
  padding: 4px 12px;
  background: linear-gradient(135deg, #f6f8f9 0%, #e9edf0 100%);
  border: 1px solid #d8dfe5;
  border-radius: 6px;
  color: #409eff;
  font-size: 13px;
  font-weight: 500;
}

.content {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.table-shell {
  position: relative;
  overflow-x: auto;
}

.order-table {
  min-width: 1260px;
}

.action-group {
  display: flex;
  gap: 8px;
  flex-wrap: nowrap;
  justify-content: flex-end;
  min-width: 208px;
}

.amount-text {
  color: #c57d1f;
  font-weight: 700;
}

.refresh-button,
.detail-button,
.ship-button {
  min-width: 96px;
  flex: 0 0 96px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px 20px;
  line-height: 1.7;
}

.full-row {
  grid-column: 1 / -1;
}

:deep(.el-table th.el-table__cell) {
  color: #7a6a55;
  background: #fbf8f2;
}

:deep(.el-table .cell) {
  line-height: 1.7;
}

:deep(.order-table .el-table__fixed-right) {
  right: 0 !important;
  height: 100% !important;
  background: #fffdf7;
  box-shadow: -1px 0 0 rgba(227, 217, 197, 0.8);
}

:deep(.order-table .el-table__fixed-right-patch) {
  width: 240px !important;
  background: #fbf8f2;
}

:deep(.order-table .el-table__fixed-right th.el-table__cell) {
  background: #fbf8f2;
}

:deep(.order-table .el-table__fixed-right td.el-table__cell) {
  background: #fffdf7;
}

:deep(.el-select) {
  width: 180px;
}

@media (max-width: 960px) {
  .card-header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions {
    justify-content: space-between;
  }
}
</style>
