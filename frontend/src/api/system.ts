import request from './request'

// 系统相关接口类型定义
export interface HealthInfo {
  status: string
  timestamp: string
  application: string
  version: string
}

export interface SystemInfo {
  applicationName: string
  version: string
  description: string
  author: string
  javaVersion: string
  springBootVersion: string
  buildTime: string
}

export interface ApiDocs {
  swagger: string
  apiPrefix: string
  userApis: string
  productApis: string
  systemApis: string
}

export interface SystemStats {
  totalProducts: number
  totalUsers: number
  totalOrders: number
  todayNewUsers: number
  todayNewOrders: number
}

export interface Product {
  productId: number
  productName: string
  brand: string
  category: string
  price: number
  stockQuantity: number
  imageUrl: string
  description: string
}

// 系统API接口
export const systemApi = {
  // 健康检查
  health: () => {
    return request.get('/system/health')
  },

  // 系统信息
  info: () => {
    return request.get('/system/info')
  },

  // API文档地址
  docs: () => {
    return request.get('/system/docs')
  },

  // 获取热销推荐商品
  getHotProducts: (limit: number = 8) => {
    return request.get<Product[]>(`/system/hot-products?limit=${limit}`)
  },

  // 获取系统统计信息
  getSystemStats: () => {
    return request.get<SystemStats>('/system/stats')
  }
} 