import { api } from './request'
import type { ApiResponse } from './request'

// 管理员相关接口类型定义
export interface Admin {
  adminId?: number
  username: string
  password?: string
  role?: 'SUPER_ADMIN' | 'ADMIN' | 'OPERATOR'
  realName?: string
  mobilePhone?: string
  email?: string
  status?: 'ACTIVE' | 'INACTIVE'
  createTime?: string
  lastLoginTime?: string
}

export interface Merchant {
  merchantId: string
  userId?: number
  username?: string
  companyName: string
  businessLicense: string
  contactPerson: string
  contactPhone: string
  businessAddress: string
  description?: string
  status?: 'PENDING' | 'APPROVED' | 'REJECTED' | 'SUSPENDED'
  createTime?: string
  approveTime?: string
}

export interface ChangePasswordData {
  adminId: number
  oldPassword: string
  newPassword: string
}

export interface DashboardStats {
  userCount: number
  merchantCount: number
  productCount: number
  orderCount: number
  pendingMerchantCount: number
  recentOrders?: any[]
  recentUsers?: any[]
}

// 管理员API接口
export const adminApi = {
  // 管理员登录
  login: (loginData: { username: string; password: string }): Promise<ApiResponse<string>> => {
    return api.post('/admin/login', loginData)
  },

  // 根据ID查询管理员
  getAdminById: (adminId: number): Promise<ApiResponse<Admin>> => {
    return api.get(`/admin/detail/${adminId}`)
  },

  // 根据用户名查询管理员
  getAdminByUsername: (username: string): Promise<ApiResponse<Admin>> => {
    return api.get(`/admin/username/${username}`)
  },

  // 查询所有管理员
  getAllAdmins: (): Promise<ApiResponse<Admin[]>> => {
    return api.get('/admin/list')
  },

  // 更新管理员信息
  updateAdmin: (adminData: Admin): Promise<ApiResponse<void>> => {
    return api.put('/admin/update', adminData)
  },

  // 修改管理员密码
  changePassword: (passwordData: ChangePasswordData): Promise<ApiResponse<void>> => {
    return api.put('/admin/change-password', passwordData)
  },

  // 获取当前管理员信息
  getCurrentAdmin: (): Promise<ApiResponse<Admin>> => {
    return api.get('/admin/current', { showError: false })
  },

  // 获取仪表盘统计数据
  getDashboardStats: (): Promise<ApiResponse<DashboardStats>> => {
    return api.get('/admin/dashboard/stats')
  },

  // 获取用户列表
  getUserList: (page = 1, size = 10): Promise<ApiResponse<any>> => {
    return api.get(`/admin/users?page=${page}&size=${size}`)
  },

  // 搜索用户
  searchUsers: (keyword: string): Promise<ApiResponse<any>> => {
    return api.get(`/admin/users/search?keyword=${keyword}`)
  },

  // 获取商家列表
  getMerchantList: (page = 1, size = 10): Promise<ApiResponse<any>> => {
    return api.get(`/admin/merchants?page=${page}&size=${size}`)
  },
  
  // 搜索商家
  searchMerchants: (searchParams: {merchantId?: string, companyName?: string}, page = 1, size = 10): Promise<ApiResponse<any>> => {
    const params = new URLSearchParams();
    params.append('page', page.toString());
    params.append('size', size.toString());
    
    if (searchParams.merchantId) {
      params.append('merchantId', searchParams.merchantId);
    }
    if (searchParams.companyName) {
      params.append('companyName', searchParams.companyName);
    }
    
    return api.get(`/admin/merchants?${params.toString()}`);
  },

  // 审核商家
  reviewMerchant: (merchantId: string, status: 'APPROVED' | 'REJECTED', reason?: string): Promise<ApiResponse<void>> => {
    return api.put('/admin/merchants/review', { merchantId, status, reason })
  },

  // 获取商品列表
  getProductList: (page = 1, size = 10): Promise<ApiResponse<any>> => {
    return api.get(`/admin/products?page=${page}&size=${size}`)
  },

  // 获取订单列表
  getOrderList: (page = 1, size = 10, status?: string): Promise<ApiResponse<any>> => {
    const statusParam = status ? `&status=${encodeURIComponent(status)}` : ''
    return api.get(`/admin/orders?page=${page}&size=${size}${statusParam}`)
  },

  // 获取订单详情
  getOrderDetail: (orderId: number): Promise<ApiResponse<any>> => {
    return api.get(`/admin/orders/detail/${orderId}`)
  },

  // 管理员取消订单
  cancelOrder: (orderId: number): Promise<ApiResponse<void>> => {
    return api.put(`/admin/orders/cancel/${orderId}`, {})
  },

  // 更新商品状态
  updateProductStatus: (productId: number, status: string): Promise<ApiResponse<void>> => {
    return api.put('/admin/products/status', { productId, status })
  },

  // 回填商品图片到对象存储
  backfillProductImages: (overwriteAll = true): Promise<ApiResponse<{ updatedCount: number; overwriteAll: boolean }>> => {
    return api.post(`/admin/products/backfill-images?overwriteAll=${overwriteAll}`, {})
  }
} 
