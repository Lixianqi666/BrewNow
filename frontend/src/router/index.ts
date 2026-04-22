import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import HomeView from '../views/HomeView.vue'
import { useUserStore } from '@/stores/user'
import AdminLayout from '../views/admin/AdminLayout.vue'
import MerchantLayout from '../views/merchant/MerchantLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 公共路由
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { title: '首页', requiresAuth: false }
    },
    {
      path: '/products',
      name: 'products',
      component: () => import('../views/ProductList.vue'),
      meta: { title: '商品列表', requiresAuth: false }
    },
    {
      path: '/product/:id',
      name: 'product-detail',
      component: () => import('../views/ProductDetail.vue'),
      meta: { title: '商品详情', requiresAuth: false }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue'),
      meta: { title: '登录注册', requiresAuth: false }
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
      meta: { title: '关于我们', requiresAuth: false }
    },

    // 仅消费者可访问的路由
    {
      path: '/cart',
      name: 'cart',
      component: () => import('../views/Cart.vue'),
      meta: { title: '购物车', requiresAuth: true, userTypes: ['consumer'] }
    },
    {
      path: '/checkout',
      name: 'checkout',
      component: () => import('../views/Checkout.vue'),
      meta: { title: '订单结算', requiresAuth: true, userTypes: ['consumer'] }
    },
    {
      path: '/orders',
      name: 'orders',
      component: () => import('../views/OrderList.vue'),
      meta: { title: '我的订单', requiresAuth: true, userTypes: ['consumer'] }
    },
    {
      path: '/orders/:id',
      name: 'order-detail',
      component: () => import('../views/OrderDetail.vue'),
      meta: { title: '订单详情', requiresAuth: true, userTypes: ['consumer'] }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/UserProfile.vue'),
      meta: { title: '个人中心', requiresAuth: true, userTypes: ['consumer'] }
    },
    {
      path: '/favorites',
      name: 'favorites',
      component: () => import('../views/FavoriteList.vue'),
      meta: { title: '我的收藏', requiresAuth: true, userTypes: ['consumer'] }
    },

    // 商家路由 - 使用商家布局
    {
      path: '/merchant',
      component: MerchantLayout,
      meta: { requiresAuth: true, userTypes: ['merchant'] },
      children: [
        {
          path: '',
          name: 'merchant-dashboard',
          component: () => import('../views/merchant/Dashboard.vue'),
          meta: { title: '商家后台', requiresAuth: true, userTypes: ['merchant'] }
        },
        {
          path: 'products',
          name: 'merchant-products',
          component: () => import('../views/merchant/ProductManage.vue'),
          meta: { title: '商品管理', requiresAuth: true, userTypes: ['merchant'] }
        },
        {
          path: 'orders',
          name: 'merchant-orders',
          component: () => import('../views/merchant/OrderManage.vue'),
          meta: { title: '商家订单', requiresAuth: true, userTypes: ['merchant'] }
        },
        {
          path: 'profile',
          name: 'merchant-profile',
          component: () => import('../views/merchant/MerchantProfile.vue'),
          meta: { title: '商家资料', requiresAuth: true, userTypes: ['merchant'] }
        }
      ]
    },

    // 管理员路由 - 使用管理员布局
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true, userTypes: ['admin'] },
      children: [
        {
          path: '',
          name: 'admin-dashboard',
          component: () => import('../views/admin/Dashboard.vue'),
          meta: { title: '管理后台', requiresAuth: true, userTypes: ['admin'] }
        },
        {
          path: 'users',
          name: 'admin-users',
          component: () => import('../views/admin/UserManage.vue'),
          meta: { title: '用户管理', requiresAuth: true, userTypes: ['admin'] }
        },
        {
          path: 'merchants',
          name: 'admin-merchants',
          component: () => import('../views/admin/MerchantManage.vue'),
          meta: { title: '商家管理', requiresAuth: true, userTypes: ['admin'] }
        },
        {
          path: 'products',
          name: 'admin-products',
          component: () => import('../views/admin/ProductManage.vue'),
          meta: { title: '商品管理', requiresAuth: true, userTypes: ['admin'] }
        },
        {
          path: 'orders',
          name: 'admin-orders',
          component: () => import('../views/admin/OrderManage.vue'),
          meta: { title: '订单管理', requiresAuth: true, userTypes: ['admin'] }
        },
        {
          path: 'recommendation',
          name: 'admin-recommendation',
          component: () => import('../views/admin/RecommendationStats.vue'),
          meta: { title: '推荐统计', requiresAuth: true, userTypes: ['admin'] }
        }
      ]
    },

    // 404页面
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/NotFound.vue'),
      meta: { title: '页面不存在' }
    }
  ],
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

  if (!userStore.initialized) {
    userStore.initUserState()
  }

  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - BrewNow 沏刻茶叶电商平台`
  }

  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn) {
      ElMessage.warning('请先登录')
      next({ name: 'login', query: { redirect: to.fullPath } })
      return
    }

    // 检查用户类型权限
    const allowedUserTypes = to.meta.userTypes as string[]
    
    if (allowedUserTypes && !allowedUserTypes.includes(userStore.userType || '')) {
      ElMessage.error('您没有权限访问此页面')
      // 根据用户类型跳转到对应的默认页面
      const defaultRoute = getDefaultRoute(userStore.userType)
      next({ name: defaultRoute })
      return
    }

    // 检查路由权限
    if (!userStore.hasRoutePermission(to.name as string)) {
      ElMessage.error('您没有权限访问此页面')
      const defaultRoute = getDefaultRoute(userStore.userType)
      next({ name: defaultRoute })
      return
    }
  }

  // 已登录用户访问登录页面，跳转到对应的默认页面
  // 但需要确保这不会干扰正常的登录流程
  if (to.name === 'login' && userStore.isLoggedIn && !to.query.redirect) {
    const defaultRoute = getDefaultRoute(userStore.userType)
    next({ name: defaultRoute })
    return
  }
  next()
})

// 获取用户类型对应的默认路由
function getDefaultRoute(userType: string | null): string {
  switch (userType) {
    case 'consumer':
      return 'home'
    case 'merchant':
      return 'merchant-dashboard'
    case 'admin':
      return 'admin-dashboard'
    default:
      return 'home'
  }
}

export default router
