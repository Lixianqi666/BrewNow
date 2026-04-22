<template>
  <div class="admin-orders">
    <el-card class="page-container">
      <template #header>
        <div class="card-header">
          <div>
            <h2>订单管理</h2>
          </div>
          <div class="header-actions">
            <el-select 
              v-model="statusFilter" 
              class="status-select"
              @change="handleStatusChange"
            >
              <el-option label="全部状态" value="ALL" />
              <el-option label="待支付" value="PENDING" />
              <el-option label="已支付" value="PAID" />
              <el-option label="已发货" value="SHIPPED" />
              <el-option label="已送达" value="DELIVERED" />
              <el-option label="已取消" value="CANCELLED" />
              <el-option label="已退款" value="REFUNDED" />
            </el-select>
            <div class="refresh-info" v-if="statusFilter !== 'ALL'">
              当前: {{ getStatusLabel(statusFilter) }}
            </div>
            <BaseButton plain :loading="loading" @click="loadOrders">刷新</BaseButton>
          </div>
        </div>
      </template>

      <div class="content">
        <div v-if="orders.length" class="table-shell">
          <el-table :data="orders" border class="admin-table brew-table" table-layout="fixed" height="600">
            <el-table-column prop="orderId" label="ID" width="72" align="center" />
            <el-table-column prop="orderNumber" label="订单号" width="220" show-overflow-tooltip align="center" />
            <el-table-column prop="userId" label="用户ID" width="92" align="center" />
            <el-table-column label="金额" width="120" align="center">
              <template #default="{ row }">￥{{ Number(row.totalAmount || 0).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="paymentMethod" label="支付方式" width="120" show-overflow-tooltip align="center" />
            <el-table-column prop="orderStatus" label="状态" width="110" align="center" />
            <el-table-column prop="orderDate" label="下单时间" width="180" show-overflow-tooltip align="center" />
            <el-table-column label="操作" width="240" align="center">
              <template #default="{ row }">
                <div class="action-group">
                  <BaseButton size="small" type="primary" @click="openDetail(row)">查看详情</BaseButton>
                  <el-tooltip :content="getCancelHint(row)" placement="top">
                    <span class="action-slot">
                      <BaseButton
                        size="small"
                        type="danger"
                        :disabled="!canCancelOrder(row)"
                        @click="cancelOrder(row)"
                      >
                        取消订单
                      </BaseButton>
                    </span>
                  </el-tooltip>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else description="暂无订单数据" />

        <div class="pagination-section" v-if="total > 0">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="订单详情" width="760px">
      <div v-if="detailData" class="detail-container">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ detailData.order.orderNumber }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ detailData.order.userId }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ detailData.order.orderStatus }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ detailData.order.paymentMethod }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ detailData.order.orderDate }}</el-descriptions-item>
          <el-descriptions-item label="金额">￥{{ Number(detailData.order.totalAmount || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ detailData.order.shippingAddress || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="联系方式" :span="2">{{ detailData.order.contactPhone || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detailData.order.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
        <el-table v-if="detailData.items.length" :data="detailData.items" border class="detail-items">
          <el-table-column prop="productNameSnapshot" label="商品名称" min-width="200" show-overflow-tooltip />
          <el-table-column prop="categorySnapshot" label="分类" width="120" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="unitPrice" label="单价" width="120">
            <template #default="{ row }">￥{{ Number(row.unitPrice || 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="subtotal" label="小计" width="120">
            <template #default="{ row }">￥{{ Number(row.subtotal || 0).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无订单明细" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const orders = ref<any[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref<string>('ALL')
const detailVisible = ref(false)
const detailData = ref<{ order: any; items: any[] } | null>(null)

const loadOrders = async () => {
  loading.value = true
  try {
    const status = statusFilter.value === 'ALL' ? undefined : statusFilter.value
    const response = await adminApi.getOrderList(page.value, pageSize.value, status)
    orders.value = response.data?.list || []
    total.value = response.data?.total || 0
  } catch (error) {
    console.error('加载管理员订单列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleStatusChange = () => {
  page.value = 1
  loadOrders()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  loadOrders()
}

const handleCurrentChange = (current: number) => {
  page.value = current
  loadOrders()
}

const openDetail = async (row: any) => {
  try {
    const response = await adminApi.getOrderDetail(row.orderId)
    if (response.code === 200) {
      detailData.value = {
        order: response.data?.order,
        items: response.data?.items || []
      }
      detailVisible.value = true
    } else {
      ElMessage.error(response.message || '获取订单详情失败')
    }
  } catch (error) {
    console.error('获取订单详情失败', error)
  }
}

const cancelOrder = async (row: any) => {
  if (!canCancelOrder(row)) {
    ElMessage.warning(getCancelHint(row))
    return
  }
  try {
    await ElMessageBox.confirm('取消后订单状态会变为已取消；若订单已支付，系统将回补库存，但不会修改订单金额。确认继续吗？', '取消订单确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await adminApi.cancelOrder(row.orderId)
    if (response.code === 200) {
      ElMessage.success('订单已取消')
      loadOrders()
    } else {
      ElMessage.error(response.message || '取消订单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败', error)
    }
  }
}

const canCancelOrder = (row: any) => row.orderStatus === 'PENDING' || row.orderStatus === 'PAID'

const getCancelHint = (row: any) => {
  if (row.orderStatus === 'CANCELLED') return '该订单已取消'
  if (row.orderStatus === 'DELIVERED' || row.orderStatus === 'SHIPPED' || row.orderStatus === 'REFUNDED') {
    return '仅待支付或已支付订单支持管理员取消'
  }
  return '管理员可取消待支付或已支付订单'
}

const getStatusLabel = (status: string) => {
  const labelMap: Record<string, string> = {
    'PENDING': '待支付',
    'PAID': '已支付',
    'SHIPPED': '已发货',
    'DELIVERED': '已送达',
    'CANCELLED': '已取消',
    'REFUNDED': '已退款'
  }
  return labelMap[status] || status
}

onMounted(loadOrders)
</script>

<style scoped>
.admin-orders {
  padding: 20px;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.card-header h2 {
  margin: 0;
  color: #303133;
}

.card-header p {
  margin: 8px 0 0;
  color: #7c6f5b;
  font-size: 13px;
}

.pagination-section {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.table-shell {
  width: 100%;
  overflow-x: auto;
}

.admin-table {
  width: 100%;
  min-width: 1100px;
}

.action-group {
  display: flex;
  flex-wrap: nowrap;
  gap: 8px;
  padding: 4px 0;
}

.action-slot {
  display: inline-flex;
}

:deep(.action-group .el-button) {
  padding: 0 16px;
}

.detail-container {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.detail-items {
  margin-top: 12px;
}
</style>
