<template>
  <div class="merchant-dashboard">
    <el-card class="page-container" v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>商家后台</h2>
          <div class="header-actions">
            <BaseButton @click="fetchDashboardStats" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新数据
            </BaseButton>
            <BaseButton plain @click="exportDashboard" :loading="exportLoading">导出数据(.xlsx)</BaseButton>
          </div>
        </div>
      </template>

      <div class="dashboard-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-icon">📦</div>
                <div class="stat-info">
                  <h3>商品总数</h3>
                  <p class="stat-number">{{ dashboardStats.productCount }}</p>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-icon">📋</div>
                <div class="stat-info">
                  <h3>订单总数</h3>
                  <p class="stat-number">{{ dashboardStats.orderCount }}</p>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <div class="welcome-message">
          <h3>欢迎使用商家管理系统</h3>
          <p>您可以在这里管理您的商品，随时修改商品价格、库存、描述等信息。</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { merchantApi } from '@/api/merchant'
import type { MerchantDashboardStats } from '@/api/merchant'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
// 加载状态
const loading = ref(false)
const exportLoading = ref(false)

// 统计数据
const dashboardStats = reactive<MerchantDashboardStats>({
  productCount: 0,
  orderCount: 0,
  totalRevenue: 0,
  customerCount: 0
})

// 获取统计数据
const fetchDashboardStats = async () => {
  if (!userStore.isLoggedIn) {
    loading.value = false
    return
  }

  loading.value = true
  try {
    console.log('开始获取商家统计数据...')
    const response = await merchantApi.getDashboardStats()
    console.log('API响应:', response)

    if (response.code === 200) {
      Object.assign(dashboardStats, response.data)
      console.log('商家统计数据获取成功:', response.data)
    } else {
      console.error('API返回错误:', response)
      ElMessage.error(response.message || '获取统计数据失败')
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败: ' + (error as Error).message)
  } finally {
    loading.value = false
  }
}

const getFilenameFromHeader = (contentDisposition?: string, fallback = 'merchant-dashboard.xlsx') => {
  if (!contentDisposition) return fallback
  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1])
  }
  const plainMatch = contentDisposition.match(/filename="?([^";]+)"?/i)
  if (plainMatch?.[1]) {
    return plainMatch[1]
  }
  return fallback
}

const triggerDownload = (blob: Blob, filename: string) => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

const exportDashboard = async () => {
  exportLoading.value = true
  try {
    const response = await merchantApi.exportDashboardStats()
    const filename = getFilenameFromHeader(
      response.headers?.['content-disposition'] as string | undefined,
      '商家数据导出.xlsx'
    )
    triggerDownload(response.data, filename)
    ElMessage.success('商家数据已导出')
  } catch (error) {
    console.error('导出商家总览失败:', error)
  } finally {
    exportLoading.value = false
  }
}

// 组件挂载时获取数据
onMounted(() => {
  if (userStore.isLoggedIn) {
    fetchDashboardStats()
  }
})
</script>

<style scoped>
.merchant-dashboard {
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
  gap: 8px;
}

.card-header h2 {
  margin: 0;
  color: #303133;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  font-size: 40px;
}

.stat-info h3 {
  margin: 0 0 8px 0;
  color: #606266;
  font-size: 14px;
}

.stat-number {
  margin: 0;
  color: #409EFF;
  font-size: 24px;
  font-weight: bold;
}

.welcome-message {
  margin-top: 30px;
  text-align: center;
  padding: 40px;
  background: #f8f9fa;
  border-radius: 8px;
}

.welcome-message h3 {
  margin: 0 0 10px 0;
  color: #303133;
}

.welcome-message p {
  margin: 0;
  color: #606266;
}
</style>
