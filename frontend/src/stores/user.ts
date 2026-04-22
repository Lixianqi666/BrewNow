import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { User } from '@/api/user'
import { userStorage } from '@/utils/storage'

export type UserType = 'consumer' | 'merchant' | 'admin'

export interface UserState {
  isLoggedIn: boolean
  userType: UserType | null
  userInfo: User | null
  token: string | null
  permissions: string[]
  merchantInfo?: {
    merchantId: string
    companyName: string
    status: string
  }
  adminInfo?: {
    role: string
    realName: string
  }
}

interface StoredUserState {
  userType: UserType
  userInfo?: User | null
  merchantInfo?: Record<string, any> | null
  adminInfo?: Record<string, any> | null
}

const getPermissionsByUserType = (type: UserType | null) => {
  switch (type) {
    case 'consumer':
      return ['view:products', 'manage:cart', 'create:order']
    case 'merchant':
      return [
        'view:products',
        'manage:products',
        'view:orders',
        'manage:orders',
        'manage:store',
        'manage:cart',
        'create:order'
      ]
    case 'admin':
      return ['view:all', 'manage:all', 'admin:users', 'admin:products', 'admin:orders']
    default:
      return []
  }
}

const parseTokenPayload = (jwtToken: string) => {
  const tokenParts = jwtToken.split('.')
  if (tokenParts.length !== 3) {
    throw new Error('Token 格式无效')
  }

  const base64 = tokenParts[1].replace(/-/g, '+').replace(/_/g, '/')
  const normalized = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')
  const payloadText = decodeURIComponent(
    atob(normalized)
      .split('')
      .map((char) => `%${`00${char.charCodeAt(0).toString(16)}`.slice(-2)}`)
      .join('')
  )

  return JSON.parse(payloadText)
}

export const useUserStore = defineStore('user', () => {
  const initialized = ref(false)
  const isLoggedIn = ref(false)
  const userType = ref<UserType | null>(null)
  const userInfo = ref<User | null>(null)
  const token = ref<string | null>(null)
  const permissions = ref<string[]>([])
  const merchantInfo = ref<Record<string, any> | null>(null)
  const adminInfo = ref<Record<string, any> | null>(null)

  const isConsumer = computed(() => userType.value === 'consumer')
  const isMerchant = computed(() => userType.value === 'merchant')
  const isAdmin = computed(() => userType.value === 'admin')

  const userName = computed(() => {
    if (userInfo.value?.username) return userInfo.value.username
    if (merchantInfo.value?.companyName) return merchantInfo.value.companyName
    if (merchantInfo.value?.merchantId) return merchantInfo.value.merchantId
    if (adminInfo.value?.realName) return adminInfo.value.realName
    if ((adminInfo.value as any)?.username) return (adminInfo.value as any).username
    return '用户'
  })

  const hasPermission = (permission: string) => permissions.value.includes(permission)
  const hasAnyPermission = (perms: string[]) => perms.some((perm) => permissions.value.includes(perm))

  const persistState = () => {
    if (!token.value || !userType.value) return

    const userStateInfo: StoredUserState = {
      userType: userType.value,
      userInfo: userInfo.value,
      merchantInfo: merchantInfo.value,
      adminInfo: adminInfo.value
    }

    userStorage.setToken(token.value)
    userStorage.setUserInfo(userStateInfo)
  }

  const resetState = () => {
    isLoggedIn.value = false
    userType.value = null
    userInfo.value = null
    token.value = null
    permissions.value = []
    merchantInfo.value = null
    adminInfo.value = null
  }

  const applyLoginState = (loginData: {
    token: string
    userType: UserType
    userInfo?: User | null
    merchantInfo?: Record<string, any> | null
    adminInfo?: Record<string, any> | null
  }) => {
    isLoggedIn.value = true
    token.value = loginData.token
    userType.value = loginData.userType
    userInfo.value = loginData.userInfo || null
    merchantInfo.value = loginData.merchantInfo || null
    adminInfo.value = loginData.adminInfo || null
    permissions.value = getPermissionsByUserType(loginData.userType)
  }

  const restoreFromStorage = () => {
    const storedToken = userStorage.getToken()
    const storedUserInfo = userStorage.getUserInfo<StoredUserState>()

    if (!storedToken || !storedUserInfo?.userType) {
      resetState()
      return
    }

    try {
      const payload = parseTokenPayload(storedToken)
      const currentTime = Math.floor(Date.now() / 1000)
      if (payload?.exp && payload.exp < currentTime) {
        throw new Error('Token 已过期')
      }
    } catch (error) {
      console.error('恢复登录态失败:', error)
      userStorage.clearUserData()
      resetState()
      return
    }

    applyLoginState({
      token: storedToken,
      userType: storedUserInfo.userType,
      userInfo: storedUserInfo.userInfo,
      merchantInfo: storedUserInfo.merchantInfo,
      adminInfo: storedUserInfo.adminInfo
    })
  }

  const login = (loginData: {
    token: string
    userType: UserType
    userInfo?: User | null
    merchantInfo?: Record<string, any> | null
    adminInfo?: Record<string, any> | null
  }) => {
    initialized.value = true
    applyLoginState(loginData)
    persistState()
  }

  const logout = () => {
    initialized.value = true
    resetState()
    userStorage.clearUserData()
  }

  const initUserState = () => {
    if (initialized.value) return
    initialized.value = true
    restoreFromStorage()
  }

  const updateUserInfo = (newUserInfo: Partial<User>) => {
    const mergedUserInfo = { ...((userInfo.value || {}) as User), ...newUserInfo } as User
    userInfo.value = mergedUserInfo
    persistState()
  }

  const updateMerchantInfo = (newMerchantInfo: Record<string, any>) => {
    merchantInfo.value = { ...(merchantInfo.value || {}), ...newMerchantInfo }
    persistState()
  }

  const updateAdminInfo = (newAdminInfo: Record<string, any>) => {
    adminInfo.value = { ...(adminInfo.value || {}), ...newAdminInfo }
    persistState()
  }

  const requireAuth = () => isLoggedIn.value

  const hasRoutePermission = (routeName: string) => {
    const routePermissions: Record<string, string[]> = {
      home: ['view:products'],
      products: ['view:products'],
      cart: ['manage:cart'],
      orders: ['create:order'],
      favorites: ['view:products'],
      'merchant-dashboard': ['manage:store'],
      'merchant-products': ['manage:products'],
      'merchant-orders': ['view:orders'],
      'merchant-profile': ['manage:store'],
      'admin-dashboard': ['admin:users'],
      'admin-users': ['admin:users'],
      'admin-merchants': ['admin:users'],
      'admin-products': ['admin:products'],
      'admin-orders': ['admin:orders'],
      'admin-recommendation': ['admin:users']
    }

    const requiredPerms = routePermissions[routeName]
    if (!requiredPerms) return true
    return hasAnyPermission(requiredPerms)
  }

  return {
    initialized,
    isLoggedIn,
    userType,
    userInfo,
    token,
    permissions,
    merchantInfo,
    adminInfo,
    isConsumer,
    isMerchant,
    isAdmin,
    userName,
    hasPermission,
    hasAnyPermission,
    login,
    logout,
    initUserState,
    updateUserInfo,
    updateMerchantInfo,
    updateAdminInfo,
    requireAuth,
    hasRoutePermission
  }
})
