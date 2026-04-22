// API统一导出
export { userApi } from './user'
export { productApi } from './product'
export { systemApi } from './system'
export { adminApi } from './admin'
export { addressApi } from './address'
export { recommendApi } from './recommend'
export { favoriteApi } from './favorite'
export { reviewApi } from './review'
export { default as request } from './request'

// 类型定义统一导出
export type { User, LoginData, ChangePasswordData } from './user'
export type { Product, ProductSearchParams } from './product'
export type { HealthInfo, SystemInfo, ApiDocs } from './system'
export type { Admin, DashboardStats, Merchant } from './admin' 
