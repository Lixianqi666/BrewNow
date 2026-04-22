<template>
  <div class="admin-users">
    <el-card class="page-container">
      <template #header>
        <div class="card-header">
          <div>
            <h2>用户管理</h2>
          </div>
          <div class="header-actions">
            <BaseButton 
              type="primary" 
              icon="RefreshRight" 
              @click="refreshUserList"
            >
              刷新
            </BaseButton>
          </div>
        </div>
      </template>

      <div class="content">
        <!-- 搜索区域 -->
        <div class="search-section">
          <el-form :model="searchForm" inline class="search-form">
            <el-form-item label="用户名">
              <el-input 
                v-model="searchForm.username" 
                placeholder="请输入用户名"
                clearable
                @keyup.enter="handleSearch"
                @clear="() => { searchForm.username = ''; handleSearch(); }"
              />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input 
                v-model="searchForm.phone" 
                placeholder="请输入手机号"
                clearable
                @keyup.enter="handleSearch"
                @clear="() => { searchForm.phone = ''; handleSearch(); }"
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
        </div>

        <!-- 用户列表 -->
        <div class="table-section">
          <div class="table-shell">
            <el-table 
              v-loading="loading" 
              :data="userList" 
              stripe 
              border
              class="admin-table brew-table"
              table-layout="fixed"
              height="600"
            >
              <el-table-column prop="userId" label="用户ID" width="82" fixed="left" align="center" />
              <el-table-column prop="account" label="账号" width="150" show-overflow-tooltip align="center" />
              <el-table-column prop="username" label="用户名" width="130" show-overflow-tooltip align="center" />
              <el-table-column prop="phone" label="手机号" width="132" align="center" />
              <el-table-column prop="email" label="邮箱" width="220" show-overflow-tooltip align="center" />
              <el-table-column prop="role" label="角色" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getRoleTagType(row.role)">
                    {{ getRoleText(row.role) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="gender" label="性别" width="86" align="center">
                <template #default="{ row }">
                  {{ getGenderText(row.gender) }}
                </template>
              </el-table-column>
              <el-table-column prop="registerTime" label="注册时间" width="180" show-overflow-tooltip align="center">
                <template #default="{ row }">
                  {{ formatDateTime(row.registerTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220" fixed="right" align="center">
                <template #default="{ row }">
                  <div class="action-group">
                    <BaseButton 
                      type="primary" 
                      size="small" 
                      @click="handleViewUser(row)"
                    >
                      查看
                    </BaseButton>
                    <BaseButton 
                      type="warning" 
                      size="small" 
                      @click="handleEditUser(row)"
                    >
                      编辑
                    </BaseButton>
                    <el-tooltip :content="getDeleteHint(row)" placement="top">
                      <span class="action-slot">
                        <BaseButton 
                          type="danger" 
                          size="small" 
                          :disabled="!canDeleteUser(row)"
                          @click="handleDeleteUser(row)"
                        >
                          删除
                        </BaseButton>
                      </span>
                    </el-tooltip>
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

    <!-- 用户详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="用户详情" width="600px">
      <div v-if="selectedUser" class="user-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ selectedUser.userId }}</el-descriptions-item>
          <el-descriptions-item label="账号">{{ selectedUser.account }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ selectedUser.username }}</el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="getRoleTagType(selectedUser.role)">
              {{ getRoleText(selectedUser.role) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="性别">{{ getGenderText(selectedUser.gender) }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ selectedUser.phone || '未设置' }}</el-descriptions-item>
          <el-descriptions-item v-if="selectedUser.role === 'MERCHANT'" label="商家ID">
            {{ selectedUser.merchantId || '未关联' }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ selectedUser.email || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ selectedUser.address || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间" :span="2">
            {{ formatDateTime(selectedUser.registerTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 编辑用户对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑用户" width="600px">
      <el-form 
        ref="editFormRef"
        :model="editForm" 
        :rules="editRules" 
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="editForm.gender" placeholder="请选择性别">
            <el-option label="男" value="MALE" />
            <el-option label="女" value="FEMALE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="editForm.address" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <BaseButton @click="editDialogVisible = false">取消</BaseButton>
          <BaseButton type="primary" @click="confirmEditUser">确定</BaseButton>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshLeft } from '@element-plus/icons-vue'
import { adminApi, userApi } from '@/api'
import type { User } from '@/api'

// 响应式数据
const loading = ref(false)
const userList = ref<User[]>([])
const detailDialogVisible = ref(false)
const editDialogVisible = ref(false)
const selectedUser = ref<User | null>(null)
const editFormRef = ref()

// 搜索表单
const searchForm = reactive({
  username: '',
  phone: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 编辑表单
const editForm = reactive({
  userId: 0,
  username: '',
  phone: '',
  email: '',
  gender: '',
  address: ''
})

// 编辑表单验证规则
const editRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
})

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    // 检查是否有搜索条件
    const hasSearchConditions = searchForm.username || searchForm.phone
    
    let response
    if (hasSearchConditions) {
      // 如果有搜索条件，使用搜索API
      console.log('使用搜索条件加载数据:', searchForm)
      const searchConditions: Partial<User> = {}
      
      if (searchForm.username) searchConditions.username = searchForm.username
      if (searchForm.phone) searchConditions.phone = searchForm.phone
      
      response = await userApi.getUsersByCondition(searchConditions)
    } else {
      // 没有搜索条件，加载所有数据
      console.log('加载所有用户数据，页码:', pagination.page, '每页数量:', pagination.size)
      response = await adminApi.getUserList(pagination.page, pagination.size)
    }
    
    if (response.code === 200 && response.data) {
      userList.value = response.data.list || response.data
      pagination.total = response.data.total || response.data.length
      console.log(`成功加载 ${userList.value.length} 条用户数据`)
    }
  } catch (error) {
    console.error('获取用户列表失败', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索用户
const handleSearch = async () => {
  pagination.page = 1
  try {
    // 构建搜索条件
    const searchConditions: Partial<User> = {}
    
    if (searchForm.username) {
      searchConditions.username = searchForm.username
    }
    
    if (searchForm.phone) {
      searchConditions.phone = searchForm.phone
    }
    
    console.log('搜索条件:', searchConditions)
    
    // 如果有任何搜索条件
    if (Object.keys(searchConditions).length > 0) {
      loading.value = true
      
      try {
        // 调用后端API进行搜索
        const response = await userApi.getUsersByCondition(searchConditions)
        if (response.code === 200 && response.data) {
          userList.value = response.data
          pagination.total = response.data.length
        }
      } catch (error) {
        console.error('搜索失败', error)
        ElMessage.error('搜索失败')
      } finally {
        loading.value = false
      }
    } else {
      // 没有搜索条件，加载所有数据
      fetchUserList()
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
    username: '',
    phone: ''
  })
  
  // 重置分页并重新获取数据
  pagination.page = 1
  pagination.size = 10
  fetchUserList()
  
  ElMessage.success('搜索条件已重置')
}

// 刷新用户列表
const refreshUserList = () => {
  fetchUserList()
}

// 查看用户详情
const handleViewUser = (user: User) => {
  selectedUser.value = user
  detailDialogVisible.value = true
}

// 编辑用户
const handleEditUser = (user: User) => {
  Object.assign(editForm, {
    userId: user.userId,
    username: user.username,
    phone: user.phone,
    email: user.email,
    gender: user.gender,
    address: user.address
  })
  editDialogVisible.value = true
}

// 确认编辑用户
const confirmEditUser = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    const response = await userApi.updateUser(editForm as User)
    if (response.code === 200) {
      ElMessage.success('用户信息更新成功')
      editDialogVisible.value = false
      fetchUserList()
    }
  } catch (error) {
    console.error('更新用户失败', error)
    ElMessage.error('更新用户失败')
  }
}

// 删除用户
const handleDeleteUser = (user: User) => {
  if (!canDeleteUser(user)) {
    ElMessage.warning(getDeleteHint(user))
    return
  }
  ElMessageBox.confirm(
    `确定要删除用户 "${user.username}" 吗？此操作不可撤销。`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const response = await userApi.deleteUser(user.userId!)
      if (response.code === 200) {
        ElMessage.success('用户删除成功')
        fetchUserList()
      }
    } catch (error) {
      console.error('删除用户失败', error)
      ElMessage.error('删除用户失败')
    }
  }).catch(() => {
    // 用户取消删除
  })
}

// 分页相关方法
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  fetchUserList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  fetchUserList()
}

// 工具方法
const getRoleTagType = (role?: string) => {
  return role === 'MERCHANT' ? 'warning' : 'success'
}

const getRoleText = (role?: string) => {
  return role === 'MERCHANT' ? '商家' : '消费者'
}

const getGenderText = (gender?: string) => {
  if (gender === 'MALE') return '男'
  if (gender === 'FEMALE') return '女'
  if (gender === 'OTHER') return '其他'
  return '未设置'
}

const canDeleteUser = (user: User) => user.role !== 'MERCHANT'

const getDeleteHint = (user: User) => {
  if (user.role === 'MERCHANT') {
    return '商家账号请在商家管理中先停用或审核处理，不建议直接删除'
  }
  return '仅建议删除普通消费者账号'
}

const formatDateTime = (dateTime?: string) => {
  if (!dateTime) return '未设置'
  return new Date(dateTime).toLocaleString()
}

onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.admin-users {
  padding: 20px;
}

.page-container {
  max-width: 1460px;
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

.table-section {
  margin-bottom: 20px;
}

.table-shell {
  overflow-x: auto;
}

.admin-table {
  min-width: 1120px;
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

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.user-detail {
  padding: 20px 0;
}

.dialog-footer {
  text-align: right;
}
</style> 
