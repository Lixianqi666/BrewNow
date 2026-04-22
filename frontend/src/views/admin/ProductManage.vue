<template>
  <div class="admin-products">
    <el-card class="page-container">
      <template #header>
        <div class="card-header">
          <h2>商品管理</h2>
          <div class="header-actions">
            <BaseButton
              type="primary"
              plain
              :loading="backfilling"
              @click="handleBackfillImages"
            >
              图片回填
            </BaseButton>
            <BaseButton plain :loading="loading" @click="loadProducts">刷新</BaseButton>
          </div>
        </div>
      </template>

      <div class="content">
        <el-table v-if="products.length" :data="products" border>
          <el-table-column prop="productId" label="ID" width="70" />
          <el-table-column prop="productName" label="茶品名称" min-width="200" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="110" />
          <el-table-column prop="originPlace" label="产地" width="120" show-overflow-tooltip />
          <el-table-column label="价格" width="120">
            <template #default="{ row }">￥{{ Number(row.price || 0).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="stockQuantity" label="库存" width="90" />
          <el-table-column prop="status" label="状态" width="110" />
          <el-table-column label="操作" min-width="120">
            <template #default="{ row }">
              <BaseButton
                :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
                size="small"
                @click="toggleStatus(row)"
              >
                {{ row.status === 'ACTIVE' ? '下架' : '上架' }}
              </BaseButton>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无商品数据" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const backfilling = ref(false)
const products = ref<any[]>([])

const loadProducts = async () => {
  if (!userStore.isLoggedIn) {
    loading.value = false
    return
  }
  
  loading.value = true
  try {
    const response = await adminApi.getProductList(1, 20)
    products.value = response.data?.list || []
  } catch (error) {
    console.error('加载管理员商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

const toggleStatus = async (row: any) => {
  if (!userStore.isLoggedIn) {
    return
  }
  
  const nextStatus = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  const action = nextStatus === 'ACTIVE' ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(`确定要${action}该商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await adminApi.updateProductStatus(row.productId, nextStatus)
    if (response.code === 200) {
      ElMessage.success(`商品已${action}`)
      loadProducts()
    } else {
      ElMessage.error(response.message || `商品${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新商品状态失败', error)
    }
  }
}

const handleBackfillImages = async () => {
  if (backfilling.value || !userStore.isLoggedIn) return
  try {
    await ElMessageBox.confirm(
      '将把商品图片同步回填到 MinIO。已存在图片也会按当前配置重新覆盖，确认继续吗？',
      '图片回填确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    backfilling.value = true
    const response = await adminApi.backfillProductImages(true)
    if (response.code === 200) {
      const updatedCount = response.data?.updatedCount ?? 0
      ElMessage.success(`图片回填完成，共处理 ${updatedCount} 条商品记录`)
      loadProducts()
    } else {
      ElMessage.error(response.message || '图片回填失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('图片回填失败', error)
      ElMessage.error('图片回填失败')
    }
  } finally {
    backfilling.value = false
  }
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    loadProducts()
  }
})
</script>

<style scoped>
.admin-products {
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

.card-header h2 {
  margin: 0;
  color: #303133;
}

:deep(.el-button--primary.is-plain) {
  color: #ffffff !important;
}

:deep(.el-button--primary.is-plain:hover) {
  color: #ffffff !important;
}
</style>
