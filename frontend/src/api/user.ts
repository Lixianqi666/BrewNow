import { api } from './request'
import type { ApiResponse } from './request'

// 用户相关接口类型定义
export interface User {
  userId?: number
  account: string
  username: string
  password?: string
  gender?: 'MALE' | 'FEMALE'
  registerTime?: string
  phone?: string
  email?: string
  address?: string
  avatarUrl?: string
  role?: 'CONSUMER' | 'MERCHANT'
  merchantId?: string
}

export interface LoginData {
  account: string
  password: string
}

export interface ChangePasswordData {
  userId: number
  oldPassword: string
  newPassword: string
}

export interface LoginResponse {
  token: string
  userInfo: User
}

export interface MerchantLoginData {
  account: string
  password: string
}

export interface AdminLoginData {
  username: string
  password: string
}

export interface MerchantRegisterData {
  username: string
  phone: string
  email: string
  password: string
  merchantId: string
  companyName: string
  contactPerson: string
  contactPhone: string
  businessAddress: string
  description?: string
}

// 用户API接口
export const userApi = {
  // 用户注册
  register: (userData: User): Promise<ApiResponse<void>> => {
    return api.post('/user/register', userData)
  },

  // 用户登录
  login: (loginData: LoginData): Promise<ApiResponse<LoginResponse>> => {
    return api.post('/user/login', loginData)
  },

  // 根据ID查询用户
  getUserById: (userId: number): Promise<ApiResponse<User>> => {
    return api.get(`/user/${userId}`)
  },

  // 根据账号查询用户
  getUserByAccount: (account: string): Promise<ApiResponse<User>> => {
    return api.get(`/user/account/${account}`)
  },

  // 查询所有用户（分页）
  getAllUsers: (page = 1, size = 10): Promise<ApiResponse<User[]>> => {
    return api.get(`/user/list?page=${page}&size=${size}`)
  },

  // 根据条件查询用户
  getUsersByCondition: (user: Partial<User>): Promise<ApiResponse<User[]>> => {
    return api.post('/user/search', user)
  },

  // 统计用户总数
  getUserCount: (): Promise<ApiResponse<number>> => {
    return api.get('/user/count')
  },

  // 更新用户信息
  updateUser: (userData: User): Promise<ApiResponse<void>> => {
    return api.put('/user/update', userData)
  },

  // 修改密码
  changePassword: (passwordData: ChangePasswordData): Promise<ApiResponse<void>> => {
    return api.put('/user/change-password', passwordData)
  },

  // 删除用户（软删除）
  deleteUser: (userId: number): Promise<ApiResponse<void>> => {
    return api.delete(`/user/${userId}`)
  },

  // 检查账号是否存在
  checkAccount: (account: string): Promise<ApiResponse<boolean>> => {
    return api.get(`/user/check-account/${account}`)
  },

  // 检查用户名是否存在
  checkUsername: (username: string): Promise<ApiResponse<boolean>> => {
    return api.get(`/user/check-username/${username}`)
  },

  // 获取当前用户信息
  getCurrentUser: (): Promise<ApiResponse<User>> => {
    return api.get('/user/current')
  },

  // 用户头像上传
  uploadAvatar: (file: File): Promise<ApiResponse<{ avatarUrl: string }>> => {
    return api.upload('/user/avatar', file)
  },

  // 商家注册
  registerMerchant: (merchantData: MerchantRegisterData): Promise<ApiResponse<void>> => {
    return api.post('/user/register-merchant', merchantData)
  },

  // 商家登录
  merchantLogin: (loginData: MerchantLoginData): Promise<ApiResponse<any>> => {
    return api.post('/user/merchant-login', loginData)
  },

  // 管理员登录
  adminLogin: (loginData: AdminLoginData): Promise<ApiResponse<string>> => {
    return api.post('/admin/login', loginData)
  },

  // 检查商家ID是否存在
  checkMerchantId: (merchantId: string): Promise<ApiResponse<boolean>> => {
    return api.get(`/user/check-merchant-id/${merchantId}`)
  }
} 
