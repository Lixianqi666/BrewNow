import request from './request'

// 商品相关接口类型定义
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
  status?: 'ACTIVE' | 'INACTIVE' | 'OUT_OF_STOCK'
  createTime?: string
  updateTime?: string
  averageRating?: number
  reviewCount?: number
}

export interface ProductSearchParams {
  keyword?: string
  category?: string
  brand?: string
  minPrice?: number
  maxPrice?: number
  status?: string
}

// 商品API接口
export const productApi = {
  // 查询所有商品（分页）
  getAllProducts: (page = 1, size = 10) => {
    return request.get(`/product/list?page=${page}&size=${size}`)
  },

  // 根据ID查询商品
  getProductById: (productId: number) => {
    return request.get(`/product/${productId}/detail`)
  },

  // 根据分类查询商品
  getProductsByCategory: (category: string) => {
    return request.get(`/product/category/${category}`)
  },

  // 搜索商品
  searchProducts: (keyword?: string, category?: string) => {
    const params = new URLSearchParams()
    if (keyword) params.append('keyword', keyword)
    if (category) params.append('category', category)
    return request.get(`/product/search?${params.toString()}`)
  },

  // 高级搜索商品
  searchProductsAdvanced: (params: ProductSearchParams) => {
    const queryString = new URLSearchParams(
      Object.entries(params)
        .filter(([, value]) => value !== undefined && value !== '')
        .map(([key, value]) => [key, String(value)])
    ).toString()
    return request.get(`/product/search/advanced?${queryString}`)
  },

  // 添加商品
  addProduct: (productData: Product) => {
    return request.post('/product/add', productData)
  },

  // 更新商品
  updateProduct: (productData: Product) => {
    return request.put('/product/update', productData)
  },

  // 删除商品
  deleteProduct: (productId: number) => {
    return request.delete(`/product/${productId}`)
  },

  // 获取商品分类列表
  getCategories: () => {
    return request.get('/product/categories')
  },

  // 获取热销商品
  getHotProducts: (limit = 8) => {
    return request.get(`/product/hot?limit=${limit}`)
  },

  // 获取推荐商品
  getRecommendedProducts: (limit = 4) => {
    return request.get(`/product/recommended?limit=${limit}`)
  }
} 
