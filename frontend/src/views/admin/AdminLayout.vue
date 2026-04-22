<template>
  <div class="admin-layout">
    <el-container class="admin-container">
      <el-aside width="230px" class="admin-aside">
        <div class="logo-container">
          <el-icon class="logo-icon"><Goblet /></el-icon>
          <div>
            <h1 class="logo-text">BrewNow</h1>
            <p class="logo-sub">管理工作台</p>
          </div>
        </div>

        <el-menu :default-active="activeMenu" class="admin-menu" router>
          <el-menu-item index="/admin">
            <el-icon><HomeFilled /></el-icon>
            <span>总览</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/merchants">
            <el-icon><Shop /></el-icon>
            <span>商家管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/products">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/orders">
            <el-icon><Tickets /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/recommendation">
            <el-icon><DataAnalysis /></el-icon>
            <span>推荐统计</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="admin-header">
          <h2>BrewNow 沏刻茶叶电商平台</h2>
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar class="header-avatar admin-avatar" size="small" :src="adminAvatar || undefined" :icon="Avatar" />
              <span class="username">{{ adminName }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-header>

        <el-main class="admin-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { HomeFilled, Avatar, Shop, Goblet, Goods, Tickets, DataAnalysis, User } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { adminApi } from '@/api'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const activeMenu = computed(() => route.path)
const adminName = computed(() => userStore.adminInfo?.realName || userStore.adminInfo?.username || '管理员')
const adminAvatar = computed(() => userStore.adminInfo?.avatarUrl || userStore.userInfo?.avatarUrl || '')

const syncCurrentAdmin = async () => {
  if (!userStore.isAdmin || !userStore.token) return
  if (userStore.adminInfo?.realName && userStore.adminInfo?.username) return
  try {
    const response = await adminApi.getCurrentAdmin()
    if (response.code === 200 && response.data) {
      userStore.updateAdminInfo(response.data)
    }
  } catch (error: any) {
    if (error?.status === 404) {
      return
    }
    console.error('同步管理员信息失败', error)
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

onMounted(() => {
  syncCurrentAdmin()
})
</script>

<style scoped>
.admin-container {
  min-height: 100vh;
}

.admin-aside {
  position: sticky;
  top: 0;
  height: 100vh;
  overflow-y: auto;
  flex-shrink: 0;
  background: linear-gradient(180deg, #2b4f3d, #1f3e2f);
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

.admin-menu {
  border-right: none;
  background: transparent;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: rgba(246, 250, 247, 0.9);
  --el-menu-hover-bg-color: rgba(255, 255, 255, 0.12);
  --el-menu-active-color: #fff7e6;
}

.admin-menu :deep(.el-menu-item) {
  color: rgba(243, 249, 245, 0.88);
  margin: 6px 10px;
  border-radius: 12px;
  font-weight: 600;
}

.admin-menu :deep(.el-menu-item .el-icon) {
  color: rgba(243, 249, 245, 0.9);
}

.admin-menu :deep(.el-menu-item.is-active) {
  color: #fff7e6;
  background: linear-gradient(135deg, rgba(199, 160, 98, 0.34), rgba(160, 111, 47, 0.2));
}

.admin-menu :deep(.el-menu-item.is-active .el-icon) {
  color: #fff7e6;
}

.admin-menu :deep(.el-menu-item:hover) {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.1);
}

.admin-header {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid #e6dcc7;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.admin-main {
  min-width: 0;
  background: linear-gradient(180deg, #f6f1e6, #f1e8d7);
  padding: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.header-avatar {
  border: 1px solid rgba(81, 122, 95, 0.22);
  box-shadow: 0 8px 22px rgba(46, 83, 62, 0.12);
}

.admin-avatar {
  background: linear-gradient(135deg, #2b4f3d, #5f8f6f);
  color: #f7f2e8;
}
</style>
