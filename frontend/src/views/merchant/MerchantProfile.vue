<template>
  <div class="merchant-profile">
    <el-card class="page-container">
      <template #header>
        <div class="card-header">
          <h2>商家信息</h2>
        </div>
      </template>

      <div class="content">
        <el-form label-width="120px">
          <el-form-item label="商家ID:">
            <span>{{ merchantIdText }}</span>
          </el-form-item>
          <el-form-item label="所属品牌:">
            <span>{{ merchantBrand }}</span>
          </el-form-item>
          <el-form-item label="企业名称:">
            <span>{{ merchantCompanyName }}</span>
          </el-form-item>
          <el-form-item label="联系人:">
            <span>{{ merchantInfo?.contactPerson || userInfo?.username || '未设置' }}</span>
          </el-form-item>
          <el-form-item label="联系电话:">
            <span>{{ merchantInfo?.contactPhone || userInfo?.phone || '未设置' }}</span>
          </el-form-item>
          <el-form-item label="经营地址:">
            <span>{{ merchantInfo?.businessAddress || userInfo?.address || '未设置' }}</span>
          </el-form-item>
          <el-form-item label="审核状态:">
            <el-tag :type="statusTagType">{{ merchantStatusLabel }}</el-tag>
          </el-form-item>
          <el-form-item label="登录账号:">
            <span>{{ userInfo?.account || merchantInfo?.merchantId || '未设置' }}</span>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { merchantApi } from '@/api/merchant'
import { useUserStore } from '@/stores/user'
import { getMerchantBrandName, getMerchantCompanyName, getMerchantStatusLabel, normalizeMerchantId } from '@/utils/merchant'

const userStore = useUserStore()

const merchantInfo = computed(() => userStore.merchantInfo || null)
const userInfo = computed(() => userStore.userInfo || null)
const merchantIdText = computed(() => normalizeMerchantId(merchantInfo.value?.merchantId || userInfo.value?.account) || '未获取')
const merchantBrand = computed(() => getMerchantBrandName(merchantInfo.value, userInfo.value?.account))
const merchantCompanyName = computed(() => getMerchantCompanyName(merchantInfo.value, userInfo.value?.account) || '未设置')
const merchantStatusLabel = computed(() => getMerchantStatusLabel(merchantInfo.value?.status))
const statusTagType = computed(() => {
  switch ((merchantInfo.value?.status || '').toUpperCase()) {
    case 'APPROVED':
      return 'success'
    case 'PENDING':
      return 'warning'
    case 'REJECTED':
      return 'danger'
    default:
      return 'info'
  }
})

const loadCurrentMerchantProfile = async () => {
  try {
    const response = await merchantApi.getCurrentMerchantProfile()
    if (response.code === 200 && response.data) {
      userStore.updateMerchantInfo(response.data)
    }
  } catch (error) {
    console.error('加载商家资料失败:', error)
  }
}

onMounted(loadCurrentMerchantProfile)
</script>

<style scoped>
.merchant-profile {
  padding: 20px;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header h2 {
  margin: 0;
  color: #303133;
}

.content {
  max-width: 760px;
}

:deep(.el-form-item__label) {
  color: #7b6a57;
}
</style>
