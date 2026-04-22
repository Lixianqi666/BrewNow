<template>
  <div class="merchant-layout">
    <el-container class="merchant-container">
      <el-aside width="230px" class="merchant-aside">
        <div class="logo-container">
          <el-icon class="logo-icon"><Goblet /></el-icon>
          <div>
            <h1 class="logo-text">BrewNow</h1>
            <p class="logo-sub">商家工作台</p>
          </div>
        </div>

        <el-menu :default-active="activeMenu" class="merchant-menu" router>
          <el-menu-item index="/merchant">
            <el-icon><HomeFilled /></el-icon>
            <span>总览</span>
          </el-menu-item>
          <el-menu-item index="/merchant/products">
            <el-icon><Goods /></el-icon>
            <span>茶品管理</span>
          </el-menu-item>
          <el-menu-item index="/merchant/orders">
            <el-icon><Tickets /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/merchant/profile">
            <el-icon><UserFilled /></el-icon>
            <span>商家资料</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="merchant-header">
          <h2>BrewNow 沏刻茶叶电商平台</h2>
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar class="header-avatar merchant-avatar" size="small" :src="merchantAvatar || undefined" :icon="OfficeBuilding" />
              <span class="username">{{ merchantName }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-header>

        <el-main class="merchant-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { HomeFilled, OfficeBuilding, Goods, Goblet, Tickets, UserFilled } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { merchantApi } from '@/api/merchant'
import { useUserStore } from '@/stores/user'
import { getMerchantCompanyName } from '@/utils/merchant'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const merchantName = computed(() => getMerchantCompanyName(userStore.merchantInfo, userStore.userInfo?.account) || '茶叶商家')
const merchantAvatar = computed(() => userStore.userInfo?.avatarUrl || '')

const loadCurrentMerchantProfile = async () => {
  if (!userStore.isMerchant) return
  try {
    const response = await merchantApi.getCurrentMerchantProfile()
    if (response.code === 200 && response.data) {
      userStore.updateMerchantInfo(response.data)
    }
  } catch (error) {
    console.error('加载商家资料失败:', error)
  }
}

const handleCommand = (command: string) => {
  if (command !== 'logout') return
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push('/login')
  }).catch(() => undefined)
}

onMounted(loadCurrentMerchantProfile)
</script>

<style scoped>
.merchant-container {
  min-height: 100vh;
}

.merchant-aside {
  position: sticky;
  top: 0;
  height: 100vh;
  overflow-y: auto;
  flex-shrink: 0;
  background: linear-gradient(180deg, #59462f, #3f2f1d);
  color: #fff;
  border-right: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-container {
  height: 72px;
  padding: 0 18px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  margin: 0;
  font-size: 18px;
}

.logo-sub {
  font-size: 12px;
  opacity: 0.8;
}

.merchant-menu {
  border-right: none;
  background: transparent;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: rgba(249, 245, 237, 0.92);
  --el-menu-hover-bg-color: rgba(255, 255, 255, 0.1);
  --el-menu-active-color: #fff3de;
}

.merchant-menu :deep(.el-menu-item) {
  color: rgba(249, 245, 237, 0.9);
  margin: 6px 10px;
  border-radius: 12px;
  font-weight: 600;
}

.merchant-menu :deep(.el-menu-item .el-icon) {
  color: rgba(249, 245, 237, 0.9);
}

.merchant-menu :deep(.el-menu-item.is-active) {
  color: #fff3de;
  background: linear-gradient(135deg, rgba(182, 133, 72, 0.32), rgba(108, 73, 31, 0.22));
}

.merchant-menu :deep(.el-menu-item.is-active .el-icon) {
  color: #fff3de;
}

.merchant-header {
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid #e6dcc7;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.merchant-main {
  min-width: 0;
  background: linear-gradient(180deg, #f7f2e8, #f1e8d9);
  padding: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.header-avatar {
  border: 1px solid rgba(124, 90, 45, 0.24);
  box-shadow: 0 8px 22px rgba(90, 58, 22, 0.14);
}

.merchant-avatar {
  background: linear-gradient(135deg, #59462f, #b68548);
  color: #fff3de;
}
</style>
