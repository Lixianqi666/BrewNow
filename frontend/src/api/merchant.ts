import request, { api } from './request'

// 商家后台统计数据类型定义
export interface MerchantDashboardStats {
  productCount: number
  orderCount: number
  totalRevenue: number
  customerCount: number
  lowStockCount?: number
  lowStockProducts?: Product[]
}

export interface MerchantProfileInfo {
  merchantId: string
  userId?: number
  companyName?: string
  businessLicense?: string
  contactPerson?: string
  contactPhone?: string
  businessAddress?: string
  description?: string
  status?: string
  createTime?: string
  approveTime?: string
}

// 商品分页查询参数
export interface MerchantProductsParams {
  page?: number
  size?: number
  keyword?: string
  category?: string
}

// 商品分页查询结果
export interface MerchantProductsResult {
  list: Product[]
  total: number
  page: number
  size: number
}

export interface MerchantOrderSummary {
  orderId: number
  orderNumber: string
  userId: number
  totalAmount: number
  orderStatus: string
  paymentMethod: string
  orderDate: string
  itemCount: number
  productNames: string
}

export interface MerchantOrderDetail {
  orderId: number
  orderNumber: string
  userId: number
  totalAmount: number
  orderStatus: string
  paymentMethod: string
  orderDate: string
  shippingAddress?: string
  contactPhone?: string
  remark?: string
  itemCount: number
  productNames: string
}

export interface MerchantOrderResult {
  list: MerchantOrderSummary[]
  total: number
  page: number
  size: number
}

// 商品接口（复用product.ts中的定义）
export interface Product {
  productId?: number
  merchantId?: string
  productName: string
  brand: string
  category: string
  teaTags?: string
  originPlace?: string
  flavorProfile?: string
  price: number
  stockQuantity: number
  warningStock?: number
  compatibleDevices?: string
  description?: string
  imageUrl?: string
  status?: 'ACTIVE' | 'INACTIVE' | 'DISCONTINUED'
  createTime?: string
  updateTime?: string
}

// 商家API接口
export const merchantApi = {
  // =============== 商家后台统计数据 ===============

  /**
   * 获取商家后台统计数据
   */
  getDashboardStats: (): Promise<any> => {
    return request.get('/merchant/dashboard/stats')
  },

  getCurrentMerchantProfile: (): Promise<any> => {
    return request.get('/merchant/profile/current', { showError: false })
  },

  // =============== 商家商品管理 ===============

  /**
   * 分页查询商家商品
   * @param params 查询参数
   */
  getMerchantProducts: (params: MerchantProductsParams = {}): Promise<any> => {
    const { page = 1, size = 10, keyword, category } = params
    const queryParams = new URLSearchParams({
      page: page.toString(),
      size: size.toString()
    })
    
    if (keyword) {
      queryParams.append('keyword', keyword)
    }
    if (category) {
      queryParams.append('category', category)
    }

    return request.get(`/merchant/products?${queryParams.toString()}`)
  },

  /**
   * 添加商品
   * @param productData 商品数据
   */
  addProduct: (productData: Product): Promise<any> => {
    return request.post('/merchant/products', productData)
  },

  /**
   * 上传商品图片
   * @param file 图片文件
   */
  uploadProductImage: (file: File): Promise<any> => {
    return api.upload('/merchant/products/upload-image', file)
  },

  /**
   * 更新商品
   * @param productId 商品ID
   * @param productData 商品数据
   */
  updateProduct: (productId: number, productData: Product): Promise<any> => {
    return request.put(`/merchant/products/${productId}`, productData)
  },

  /**
   * 删除商品
   * @param productId 商品ID
   */
  deleteProduct: (productId: number): Promise<any> => {
    return request.delete(`/merchant/products/${productId}`, { showError: false })
  },

  /**
   * 获取商品详情
   * @param productId 商品ID
   */
  getProductDetail: (productId: number): Promise<any> => {
    return request.get(`/merchant/products/${productId}`)
  },

  /**
   * 更新商品状态
   * @param productId 商品ID
   * @param status 新状态
   */
  updateProductStatus: (productId: number, status: string): Promise<any> => {
    return request.put(`/merchant/products/${productId}/status?status=${status}`, undefined, { showError: false })
  },

  // =============== 商家订单管理 ===============

  getMerchantOrders: (page = 1, size = 10, status?: string): Promise<any> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString()
    })
    if (status) {
      params.append('status', status)
    }
    return request.get(`/merchant/orders?${params.toString()}`, { showError: false })
  },

  getMerchantOrderDetail: (orderId: number): Promise<any> => {
    return request.get(`/merchant/orders/${orderId}`, { showError: false })
  },

  shipMerchantOrder: (orderId: number): Promise<any> => {
    return request.put(`/merchant/orders/${orderId}/ship`, undefined, { showError: false })
  },

  // =============== 辅助方法 ===============

  /**
   * 获取商品分类列表（可以复用通用接口）
   */
  getCategories: (): Promise<any> => {
    return request.get('/product/categories')
  },

  /**
   * 批量更新商品状态
   * @param productIds 商品ID数组
   * @param status 新状态
   */
  batchUpdateProductStatus: async (productIds: number[], status: string): Promise<any> => {
    const promises = productIds.map(id => 
      merchantApi.updateProductStatus(id, status)
    )
    return Promise.all(promises)
  },

  /**
   * 批量删除商品
   * @param productIds 商品ID数组
   */
  batchDeleteProducts: async (productIds: number[]): Promise<any> => {
    const promises = productIds.map(id => 
      merchantApi.deleteProduct(id)
    )
    return Promise.all(promises)
  }
}

// 导出默认对象
export default merchantApi

// 商品状态枚举
export const ProductStatus = {
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE', 
  DISCONTINUED: 'DISCONTINUED'
} as const

// 商品分类枚举
export const ProductCategory = {
  GREEN_TEA: '绿茶',
  BLACK_TEA: '红茶',
  OOLONG_TEA: '乌龙茶',
  WHITE_TEA: '白茶',
  FLOWER_TEA: '花茶',
  PUER_TEA: '普洱茶'
} as const

// 商品分类标签映射（数据库中使用中文分类名，这里直接映射显示名称）
export const CategoryLabels = {
  '绿茶': '绿茶',
  '红茶': '红茶',
  '乌龙茶': '乌龙茶',
  '白茶': '白茶',
  '花茶': '花茶',
  '普洱茶': '普洱茶'
} as const

// 商品状态标签映射
export const StatusLabels = {
  [ProductStatus.ACTIVE]: '上架',
  [ProductStatus.INACTIVE]: '下架',
  [ProductStatus.DISCONTINUED]: '停产'
} as const 
