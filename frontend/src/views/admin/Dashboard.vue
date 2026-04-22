<template>
  <div class="admin-dashboard">
    <el-card class="page-container">
      <template #header>
        <div class="card-header">
          <h2>管理后台</h2>
        </div>
      </template>

      <div class="dashboard-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-icon"><el-icon><UserFilled /></el-icon></div>
                <div class="stat-info">
                  <h3>用户总数</h3>
                  <p class="stat-number">{{ dashboardStats.userCount }}</p>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-item">
                <div class="stat-icon"><el-icon><OfficeBuilding /></el-icon></div>
                <div class="stat-info">
                  <h3>商家总数</h3>
                  <p class="stat-number">{{ dashboardStats.merchantCount }}</p>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 待审核商家提示 -->
        <el-alert
          v-if="dashboardStats.pendingMerchantCount > 0"
          type="warning"
          :closable="false"
          class="pending-alert"
        >
          <template #title>
            <div class="pending-title">
              有 <span class="pending-count">{{ dashboardStats.pendingMerchantCount }}</span> 个商家正在等待审核
              <BaseButton 
                type="primary" 
                size="small" 
                @click="goToMerchantManage"
                class="pending-button"
              >
                前往审核
              </BaseButton>
            </div>
          </template>
        </el-alert>

        <!-- 系统状态 -->
        <el-row :gutter="20" class="system-row">
          <el-col :span="12">
            <el-card shadow="hover" class="system-card">
              <template #header>
                <div class="card-header">
                  <h3>系统信息</h3>
                </div>
              </template>
              <div v-loading="systemLoading">
                <div v-if="systemInfo" class="system-info">
                  <p><strong>应用名称：</strong>{{ systemInfo.applicationName }}</p>
                  <p><strong>版本：</strong>{{ systemInfo.version }}</p>
                  <p><strong>Java版本：</strong>{{ systemInfo.javaVersion }}</p>
                  <p><strong>SpringBoot版本：</strong>{{ systemInfo.springBootVersion }}</p>
                  <p><strong>构建时间：</strong>{{ formatDate(systemInfo.buildTime) }}</p>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" class="system-card">
              <template #header>
                <div class="card-header">
                  <h3>功能导航</h3>
                </div>
              </template>
              <div class="quick-nav">
                <BaseButton 
                  type="primary" 
                  @click="goTo('admin-users')"
                  class="nav-button"
                >
                  用户管理
                </BaseButton>
                <BaseButton 
                  type="primary" 
                  @click="goTo('admin-merchants')"
                  class="nav-button"
                >
                  商家管理
                </BaseButton>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi, systemApi } from '@/api'
import type { DashboardStats, SystemInfo } from '@/api'
import { ElMessage } from 'element-plus'
import { UserFilled, OfficeBuilding } from '@element-plus/icons-vue'

const router = useRouter()
const systemLoading = ref(false)
const systemInfo = ref<SystemInfo | null>(null)

// 仪表盘统计数据
const dashboardStats = reactive<DashboardStats>({
  userCount: 0,
  merchantCount: 0,
  productCount: 0,
  orderCount: 0,
  pendingMerchantCount: 0
})

// 获取仪表盘数据
const fetchDashboardData = async () => {
  try {
    const response = await adminApi.getDashboardStats()
    if (response.code === 200 && response.data) {
      Object.assign(dashboardStats, response.data)
    }
  } catch (error) {
    console.error('获取仪表盘数据失败', error)
    ElMessage.error('获取仪表盘数据失败')
    
    // 设置模拟数据，实际项目中应删除这部分
    Object.assign(dashboardStats, {
      userCount: 1256,
      merchantCount: 48,
      productCount: 324,
      orderCount: 987,
      pendingMerchantCount: 5
    })
  }
}

// 获取系统信息
const fetchSystemInfo = async () => {
  systemLoading.value = true
  try {
    const response = await systemApi.info()
    if (response && response.data) {
      systemInfo.value = response.data
    }
  } catch (error) {
    console.error('获取系统信息失败', error)
  } finally {
    systemLoading.value = false
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

// 页面跳转
const goTo = (routeName: string) => {
  router.push({ name: routeName })
}

// 跳转到商家管理
const goToMerchantManage = () => {
  router.push({ name: 'admin-merchants' })
}

onMounted(() => {
  fetchDashboardData()
  fetchSystemInfo()
})
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header h2 {
  margin: 0;
  color: #303133;
  font-size: 20px;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.stat-card {
  margin-bottom: 20px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #e9f3eb, #d7e8da);
  color: #3a654d;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
}

.stat-info h3 {
  margin: 0 0 8px 0;
  color: #606266;
  font-size: 14px;
}

.stat-number {
  margin: 0;
  color: #409EFF;
  font-size: 28px;
  font-weight: bold;
}

.pending-alert {
  margin: 20px 0;
}

.pending-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.pending-count {
  font-weight: bold;
  color: #E6A23C;
  font-size: 18px;
  margin: 0 5px;
}

.pending-button {
  margin-left: 20px;
}

.system-row {
  margin-top: 20px;
}

.system-card {
  height: 100%;
  margin-bottom: 20px;
}

.system-info p {
  margin: 10px 0;
  color: #606266;
}

.quick-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  justify-content: center;
  padding: 10px;
}

.nav-button {
  width: 120px;
}
</style> 
