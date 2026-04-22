<template>
  <div class="admin-merchants">
    <el-card class="page-container">
      <template #header>
        <div class="card-header">
          <div>
            <h2>商家管理</h2>
          </div>
          <div class="header-actions">
            <BaseButton
              type="primary"
              icon="RefreshRight"
              @click="refreshMerchantList"
            >
              刷新
            </BaseButton>
          </div>
        </div>
      </template>

      <div class="content">
        <!-- 搜索和筛选区域 -->
        <div class="search-section">
          <el-form :model="searchForm" inline class="search-form">
            <el-form-item label="商家ID">
              <el-input
                v-model="searchForm.merchantId"
                placeholder="请输入商家ID"
                clearable
                @keyup.enter="handleSearch"
                @clear="() => { searchForm.merchantId = ''; handleSearch(); }"
              />
            </el-form-item>
            <el-form-item label="公司名称">
              <el-input
                v-model="searchForm.companyName"
                placeholder="请输入公司名称"
                clearable
                @keyup.enter="handleSearch"
                @clear="() => { searchForm.companyName = ''; handleSearch(); }"
              />
            </el-form-item>
            <el-form-item>
              <BaseButton class="search-btn" type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon> 搜索
              </BaseButton>
              <BaseButton class="reset-btn" @click="resetSearch">
                <el-icon><RefreshLeft /></el-icon> 重置
              </BaseButton>
            </el-form-item>
          </el-form>

          <!-- 显示当前搜索条件 -->
          <div v-if="hasActiveFilters" class="active-filters">
            <span class="filter-label">当前筛选条件:</span>
            <el-tag
              v-if="searchForm.merchantId"
              closable
              @close="clearFilter('merchantId')"
            >
              商家ID: {{ searchForm.merchantId }}
            </el-tag>
            <el-tag
              v-if="searchForm.companyName"
              closable
              @close="clearFilter('companyName')"
            >
              公司名称: {{ searchForm.companyName }}
            </el-tag>
          </div>
        </div>

        <!-- 统计信息 -->
        <div class="stats-section">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-item">
                  <div class="stat-icon pending">📋</div>
                  <div class="stat-info">
                    <h3>待审核</h3>
                    <p class="stat-number">{{ stats.pending }}</p>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-item">
                  <div class="stat-icon approved">✅</div>
                  <div class="stat-info">
                    <h3>已通过</h3>
                    <p class="stat-number">{{ stats.approved }}</p>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-item">
                  <div class="stat-icon rejected">❌</div>
                  <div class="stat-info">
                    <h3>已拒绝</h3>
                    <p class="stat-number">{{ stats.rejected }}</p>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-item">
                  <div class="stat-icon suspended">⏸️</div>
                  <div class="stat-info">
                    <h3>已暂停</h3>
                    <p class="stat-number">{{ stats.suspended }}</p>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 商家列表 -->
        <div class="table-section">
          <div class="table-shell">
            <el-table
              v-loading="loading"
              :data="merchantList"
              stripe
              border
              class="admin-table brew-table"
              table-layout="fixed"
            >
              <el-table-column prop="merchantId" label="商家ID" width="120" align="center" />
              <el-table-column label="用户名称" width="130" show-overflow-tooltip align="center">
                <template #default="{ row }">
                  <template v-if="row.userId">
                    <el-link type="primary" @click="viewUserDetails(row.userId)">
                      <username-display :user-id="row.userId" />
                    </el-link>
                  </template>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column prop="companyName" label="公司名称" width="180" show-overflow-tooltip align="center" />
              <el-table-column prop="contactPerson" label="联系人" width="110" align="center" />
              <el-table-column prop="contactPhone" label="联系电话" width="130" align="center" />
              <el-table-column prop="businessAddress" label="经营地址" width="220" show-overflow-tooltip align="center" />
              <el-table-column prop="status" label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getStatusTagType(row.status)">
                    {{ getStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="申请时间" width="180" show-overflow-tooltip align="center">
                <template #default="{ row }">
                  {{ formatDateTime(row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="approveTime" label="审核时间" width="180" show-overflow-tooltip align="center">
                <template #default="{ row }">
                  {{ formatDateTime(row.approveTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="300" align="center">
                <template #default="{ row }">
                  <div class="action-group">
                    <BaseButton
                      type="primary"
                      size="small"
                      @click="handleViewMerchant(row)"
                    >
                      查看
                    </BaseButton>
                    <BaseButton
                      v-if="row.status === 'PENDING'"
                      type="success"
                      size="small"
                      @click="handleApproveMerchant(row)"
                    >
                      通过
                    </BaseButton>
                    <BaseButton
                      v-if="row.status === 'PENDING'"
                      type="danger"
                      size="small"
                      @click="handleRejectMerchant(row)"
                    >
                      拒绝
                    </BaseButton>
                    <BaseButton
                      v-if="row.status === 'APPROVED'"
                      type="warning"
                      size="small"
                      @click="handleSuspendMerchant(row)"
                    >
                      暂停
                    </BaseButton>
                    <BaseButton
                      v-if="row.status === 'SUSPENDED'"
                      type="success"
                      size="small"
                      @click="handleActivateMerchant(row)"
                    >
                      恢复
                    </BaseButton>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 分页 -->
          <div class="pagination-section">
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.size"
              :page-sizes="[10, 20, 50, 100]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </div>
    </el-card>

    <!-- 商家详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="商家详情" width="700px">
      <div v-if="selectedMerchant" class="merchant-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商家ID">{{ selectedMerchant.merchantId }}</el-descriptions-item>
          <el-descriptions-item label="关联用户">
            <template v-if="selectedMerchant.userId">
              <username-display :user-id="selectedMerchant.userId" />
            </template>
            <template v-else>未关联</template>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(selectedMerchant.status)">
              {{ getStatusText(selectedMerchant.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="公司名称">{{ selectedMerchant.companyName }}</el-descriptions-item>
          <el-descriptions-item label="营业执照号">{{ selectedMerchant.businessLicense }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ selectedMerchant.contactPerson }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ selectedMerchant.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="经营地址" :span="2">{{ selectedMerchant.businessAddress }}</el-descriptions-item>
          <el-descriptions-item label="商家描述" :span="2">
            {{ selectedMerchant.description || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">
            {{ formatDateTime(selectedMerchant.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="审核时间">
            {{ formatDateTime(selectedMerchant.approveTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="reviewDialogVisible" :title="reviewTitle" width="500px">
      <el-form :model="reviewForm" label-width="100px">
        <el-form-item label="商家ID">
          <el-input v-model="reviewForm.merchantId" disabled />
        </el-form-item>
        <el-form-item label="公司名称">
          <el-input v-model="reviewForm.companyName" disabled />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-tag :type="getStatusTagType(reviewForm.status)">
            {{ getStatusText(reviewForm.status) }}
          </el-tag>
        </el-form-item>
        <el-form-item v-if="reviewForm.status === 'REJECTED'" label="拒绝原因">
          <el-input
            v-model="reviewForm.reason"
            type="textarea"
            rows="3"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <BaseButton @click="reviewDialogVisible = false">取消</BaseButton>
          <BaseButton type="primary" @click="confirmReview">确定</BaseButton>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi, userApi } from '@/api'
import type { Merchant } from '@/api'
import UsernameDisplay from '@/components/UsernameDisplay.vue'

// 响应式数据
const loading = ref(false)
const merchantList = ref<Merchant[]>([])
const detailDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const selectedMerchant = ref<Merchant | null>(null)

// 搜索表单
const searchForm = reactive({
  merchantId: '',
  companyName: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 审核表单
const reviewForm = reactive({
  merchantId: '',
  companyName: '',
  status: '',
  reason: ''
})

// 统计数据
const stats = computed(() => {
  const pending = merchantList.value.filter(m => m.status === 'PENDING').length
  const approved = merchantList.value.filter(m => m.status === 'APPROVED').length
  const rejected = merchantList.value.filter(m => m.status === 'REJECTED').length
  const suspended = merchantList.value.filter(m => m.status === 'SUSPENDED').length

  return { pending, approved, rejected, suspended }
})

// 判断是否有活跃的筛选条件
const hasActiveFilters = computed(() => {
  return !!(searchForm.merchantId || searchForm.companyName)
})

// 审核对话框标题
const reviewTitle = computed(() => {
  switch (reviewForm.status) {
    case 'APPROVED': return '通过审核'
    case 'REJECTED': return '拒绝审核'
    case 'SUSPENDED': return '暂停商家'
    default: return '恢复商家'
  }
})

// 获取商家列表
const fetchMerchantList = async () => {
  loading.value = true
  try {
    const response = await adminApi.getMerchantList(pagination.page, pagination.size)
    if (response.code === 200 && response.data) {
      merchantList.value = response.data.list || response.data
      pagination.total = response.data.total || response.data.length
    }
  } catch (error) {
    console.error('获取商家列表失败', error)
    ElMessage.error('获取商家列表失败')

    // 后端获取数据失败，展示错误提示
    ElMessage.error('获取商家列表失败，请稍后重试')
    merchantList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索商家
const handleSearch = async () => {
  pagination.page = 1

  try {
    // 构建搜索条件
    const searchConditions: Partial<Merchant> = {}

    if (searchForm.merchantId) {
      searchConditions.merchantId = searchForm.merchantId
    }

    if (searchForm.companyName) {
      searchConditions.companyName = searchForm.companyName
    }

    console.log('搜索条件:', searchConditions)

    // 如果有任何搜索条件
    if (Object.keys(searchConditions).length > 0) {
      loading.value = true

      try {
        // 使用搜索商家接口 - 后端应实现通过查询参数筛选
        const response = await adminApi.searchMerchants(searchConditions, pagination.page, pagination.size)
                  if (response.code === 200 && response.data) {
            let filteredData = response.data.list || response.data

            // 前端过滤实现搜索功能
            if (searchConditions.merchantId || searchConditions.companyName) {
              filteredData = filteredData.filter((item: Merchant) => {
                let match = true
                if (searchConditions.merchantId && !item.merchantId.toLowerCase().includes(searchConditions.merchantId.toLowerCase())) {
                  match = false
                }
                if (searchConditions.companyName && !item.companyName.toLowerCase().includes(searchConditions.companyName.toLowerCase())) {
                  match = false
                }
                return match
              })
            }

            merchantList.value = filteredData
            pagination.total = filteredData.length
            ElMessage.success(`找到 ${merchantList.value.length} 条商家记录`)
          }
      } catch (error) {
        console.error('搜索失败', error)
        ElMessage.error('搜索失败')
      } finally {
        loading.value = false
      }
    } else {
      // 没有搜索条件，重新加载所有数据
      fetchMerchantList()
    }
  } catch (error) {
    console.error('搜索处理错误', error)
    ElMessage.error('搜索处理错误')
  }
}

// 重置搜索
const resetSearch = () => {
  // 清空所有搜索条件
  Object.assign(searchForm, {
    merchantId: '',
    companyName: ''
  })

  // 重置分页并重新获取数据
  pagination.page = 1
  pagination.size = 10
  fetchMerchantList()

  ElMessage.success('搜索条件已重置')
}

// 清除单个筛选条件
const clearFilter = (field: 'merchantId' | 'companyName') => {
  searchForm[field] = ''
  handleSearch()
}

// 刷新商家列表
const refreshMerchantList = () => {
  fetchMerchantList()
}

// 查看商家详情
const handleViewMerchant = (merchant: Merchant) => {
  selectedMerchant.value = merchant
  detailDialogVisible.value = true
}

// 审核商家 - 通过
const handleApproveMerchant = (merchant: Merchant) => {
  Object.assign(reviewForm, {
    merchantId: merchant.merchantId,
    companyName: merchant.companyName,
    status: 'APPROVED',
    reason: ''
  })
  reviewDialogVisible.value = true
}

// 审核商家 - 拒绝
const handleRejectMerchant = (merchant: Merchant) => {
  Object.assign(reviewForm, {
    merchantId: merchant.merchantId,
    companyName: merchant.companyName,
    status: 'REJECTED',
    reason: ''
  })
  reviewDialogVisible.value = true
}

// 暂停商家
const handleSuspendMerchant = (merchant: Merchant) => {
  Object.assign(reviewForm, {
    merchantId: merchant.merchantId,
    companyName: merchant.companyName,
    status: 'SUSPENDED',
    reason: ''
  })
  reviewDialogVisible.value = true
}

// 恢复商家
const handleActivateMerchant = (merchant: Merchant) => {
  Object.assign(reviewForm, {
    merchantId: merchant.merchantId,
    companyName: merchant.companyName,
    status: 'APPROVED',
    reason: ''
  })
  reviewDialogVisible.value = true
}

// 确认审核
const confirmReview = async () => {
  if (reviewForm.status === 'REJECTED' && !reviewForm.reason.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  try {
    const response = await adminApi.reviewMerchant(
      reviewForm.merchantId,
      reviewForm.status as 'APPROVED' | 'REJECTED',
      reviewForm.reason
    )
    if (response.code === 200) {
      ElMessage.success('商家审核成功')
      reviewDialogVisible.value = false
      fetchMerchantList()
    }
  } catch (error) {
    console.error('商家审核失败', error)
    ElMessage.error('商家审核失败')
  }
}

// 分页相关方法
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  fetchMerchantList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  fetchMerchantList()
}

// 工具方法
const getStatusTagType = (status?: string) => {
  switch (status) {
    case 'PENDING': return 'warning'
    case 'APPROVED': return 'success'
    case 'REJECTED': return 'danger'
    case 'SUSPENDED': return 'info'
    default: return ''
  }
}

const getStatusText = (status?: string) => {
  switch (status) {
    case 'PENDING': return '待审核'
    case 'APPROVED': return '已通过'
    case 'REJECTED': return '已拒绝'
    case 'SUSPENDED': return '已暂停'
    default: return '未知'
  }
}

const formatDateTime = (dateTime?: string) => {
  if (!dateTime) return '无'
  return new Date(dateTime).toLocaleString()
}

// 查看关联用户详情
const viewUserDetails = async (userId: number) => {
  if (!userId) return

  try {
    const response = await userApi.getUserById(userId)
    if (response.code === 200 && response.data) {
      const user = response.data

      ElMessageBox.alert(
        `用户ID: ${user.userId}<br>
         用户名: ${user.username}<br>
         账号: ${user.account}<br>
         手机: ${user.phone || '未设置'}<br>
         邮箱: ${user.email || '未设置'}<br>
         注册时间: ${user.registerTime ? new Date(user.registerTime).toLocaleString() : '未知'}`,
        '用户详情',
        {
          confirmButtonText: '确定',
          dangerouslyUseHTMLString: true
        }
      )
    } else {
      ElMessage.error('获取用户详情失败')
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
    ElMessage.error('获取用户详情失败')
  }
}

onMounted(() => {
  fetchMerchantList()
})
</script>

<style scoped>
.admin-merchants {
  padding: 20px;
}

.page-container {
  max-width: 1560px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #303133;
  font-size: 20px;
}

.page-tip {
  margin: 8px 0 0;
  color: #7c6f5b;
  font-size: 13px;
}

.search-section {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.search-form {
  margin: 0;
}

.search-form :deep(.el-input) {
  width: 180px;
}

.search-form :deep(.el-input__wrapper) {
  height: 32px;
  line-height: 30px;
}

.search-form :deep(.el-input__inner) {
  height: 30px;
  line-height: 30px;
}

.search-btn,
.reset-btn {
  height: 32px !important;
  padding: 0 16px !important;
  font-size: 13px;
}

.search-btn :deep(.el-icon) {
  margin-right: 4px;
}

.reset-btn :deep(.el-icon) {
  margin-right: 4px;
}

.active-filters {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.filter-label {
  color: #606266;
  font-size: 14px;
}

.el-tag {
  margin-right: 5px;
}

.stats-section {
  margin-bottom: 20px;
}

.stat-card {
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
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  background: #f5efe4;
}

.stat-icon.pending {
  color: #E6A23C;
  background: rgba(230, 162, 60, 0.12);
}

.stat-icon.approved {
  color: #67C23A;
  background: rgba(103, 194, 58, 0.12);
}

.stat-icon.rejected {
  color: #F56C6C;
  background: rgba(245, 108, 108, 0.12);
}

.stat-icon.suspended {
  color: #909399;
  background: rgba(144, 147, 153, 0.14);
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

.table-section {
  margin-bottom: 20px;
}

.table-shell {
  width: 100%;
  overflow-x: auto;
}

.admin-table {
  width: 100%;
  min-width: 1600px;
}

.action-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.merchant-detail {
  padding: 20px 0;
}

.dialog-footer {
  text-align: right;
}
</style>
