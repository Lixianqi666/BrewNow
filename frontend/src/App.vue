<script setup lang="ts">
import { computed, onMounted, watch, type Component } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Setting, Goods, Star } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { useOrderStore } from '@/stores/order'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()
const orderStore = useOrderStore()

const isBackendRoute = computed(() => {
  const path = route.path || ''
  return path.startsWith('/admin') || path.startsWith('/merchant')
})

const navMenus = computed(() => {
  const menus: Array<{ name: string; path: string; show: boolean; badge?: number }> = []

  if (userStore.isLoggedIn === false || userStore.isConsumer) {
    menus.push(
      { name: '首页', path: '/', show: true },
      { name: '茶品商城', path: '/products', show: true }
    )

    if (userStore.isConsumer) {
      menus.push({ name: '购物车', path: '/cart', show: true, badge: cartStore.cartCount })
    }
  }

  if (userStore.isMerchant) {
    menus.push(
      { name: '商家工作台', path: '/merchant', show: true },
      { name: '茶品管理', path: '/merchant/products', show: true },
      { name: '订单管理', path: '/merchant/orders', show: true }
    )
  }

  if (userStore.isAdmin) {
    menus.push(
      { name: '管理工作台', path: '/admin', show: true },
      { name: '用户管理', path: '/admin/users', show: true },
      { name: '商家管理', path: '/admin/merchants', show: true },
      { name: '茶品管理', path: '/admin/products', show: true },
      { name: '订单管理', path: '/admin/orders', show: true }
    )
  }

  return menus.filter(menu => menu.show)
})

const userMenus = computed(() => {
  if (userStore.isLoggedIn === false) return []

  const menus: Array<{ name: string; path: string; icon: Component }> = []

  if (userStore.isConsumer) {
    menus.push(
      { name: '我的订单', path: '/orders', icon: Goods },
      { name: '我的收藏', path: '/favorites', icon: Star },
      { name: '个人中心', path: '/profile', icon: User }
    )
  } else if (userStore.isMerchant) {
    menus.push(
      { name: '商家订单', path: '/merchant/orders', icon: Goods },
      { name: '商家信息', path: '/merchant/profile', icon: Setting }
    )
  } else if (userStore.isAdmin) {
    menus.push(
      { name: '系统订单', path: '/admin/orders', icon: Goods },
      { name: '系统设置', path: '/admin/settings', icon: Setting }
    )
  }

  return menus
})

const userAvatar = computed(() => userStore.userInfo?.avatarUrl || '')

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
  } catch {
    // ignore
  }
}

const handleUserCommand = (command: string) => {
  if (command === 'logout') {
    handleLogout()
  } else {
    router.push(command)
  }
}

const getUserTypeColor = (userType: string) => {
  switch (userType) {
    case 'consumer':
      return 'success'
    case 'merchant':
      return 'warning'
    case 'admin':
      return 'danger'
    default:
      return 'info'
  }
}

const getUserTypeText = (userType: string) => {
  switch (userType) {
    case 'consumer':
      return '消费者'
    case 'merchant':
      return '商家'
    case 'admin':
      return '管理员'
    default:
      return '用户'
  }
}

const handleLoginClick = () => {
  if (userStore.isLoggedIn) {
    userStore.logout()
  }
  router.push('/login')
}

watch(
  () => userStore.isLoggedIn,
  (isLoggedIn) => {
    if (isLoggedIn) {
      cartStore.onUserLogin()
      orderStore.onUserLogin()
    } else {
      cartStore.onUserLogout()
      orderStore.onUserLogout()
    }
  }
)

onMounted(() => {
  userStore.initUserState()
})
</script>

<template>
  <div id="app" class="app-shell">
    <header class="main-header" v-show="!isBackendRoute">
      <div class="header-content">
        <div class="logo" @click="router.push('/')">
          <img src="@/assets/logo.svg" alt="沏刻" class="logo-icon" />
          <div>
            <strong>BrewNow</strong>
            <span>沏刻茶叶电商平台</span>
          </div>
        </div>
        <div class="nav-menu">
          <router-link
            v-for="menu in navMenus"
            :key="menu.path"
            :to="menu.path"
            class="nav-link"
          >
            <span>{{ menu.name }}</span>
            <el-badge v-if="menu.badge && menu.badge > 0" :value="menu.badge" class="nav-badge" />
          </router-link>

          <div v-if="userStore.isLoggedIn" class="user-menu">
            <el-dropdown @command="handleUserCommand" placement="bottom-end">
              <div class="user-info">
                <el-avatar :size="28" :src="userAvatar" :icon="User" />
                <span class="username">{{ userStore.userName }}</span>
                <el-tag
                  v-if="userStore.userType"
                  :type="getUserTypeColor(userStore.userType)"
                  size="small"
                  class="user-type-tag"
                >
                  {{ getUserTypeText(userStore.userType) }}
                </el-tag>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="menu in userMenus" :key="menu.path" :command="menu.path">
                    <el-icon>
                      <component :is="menu.icon" />
                    </el-icon>
                    {{ menu.name }}
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><Setting /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <div v-else class="nav-link login-btn" @click="handleLoginClick">
            <el-icon><User /></el-icon>
            登录
          </div>
        </div>
      </div>
    </header>

    <main :class="['main-content', { 'main-content-full': isBackendRoute }]">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #faf5eb;
}

.main-header {
  background-color: #2b5a44;
  background: linear-gradient(120deg, #214a38, #3f6f57 58%, #9a6c35);
  -webkit-backdrop-filter: blur(8px);
  backdrop-filter: blur(8px);
  color: #fff;
  height: 68px;
  min-height: 68px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
  position: sticky;
  top: 0;
  z-index: 20;
}

.header-content {
  width: min(1400px, 92vw);
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 26px;
  cursor: pointer;
}

.logo-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  flex-shrink: 0;
  transition: transform 0.3s ease;
}

.logo:hover .logo-icon {
  transform: scale(1.08);
}

.logo strong {
  display: block;
  font-size: 18px;
  letter-spacing: 0.06em;
}

.logo span {
  display: block;
  font-size: 12px;
  opacity: 0.9;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-link {
  color: #fff;
  padding: 8px 14px;
  border-radius: 999px;
  border: 1px solid transparent;
  transition: all 0.2s ease;
}

.nav-link:hover,
.nav-link.router-link-active {
  background: rgba(255, 255, 255, 0.13);
  border-color: rgba(255, 255, 255, 0.3);
}

.login-btn {
  background: rgba(255, 255, 255, 0.16);
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
}

.main-content {
  flex: 1;
  width: 100%;
  min-height: calc(100vh - 68px);
  background-color: #faf5eb;
  overflow-x: hidden;
}

.main-content-full {
  min-height: 100vh;
  background: transparent;
}
</style>
