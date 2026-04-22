import request from './request'

// 订单状态枚举
export enum OrderStatus {
  PENDING = 'PENDING',     // 待支付
  PAID = 'PAID',          // 已支付
  SHIPPED = 'SHIPPED',    // 已发货
  DELIVERED = 'DELIVERED', // 已送达
  CANCELLED = 'CANCELLED', // 已取消
  REFUNDED = 'REFUNDED'   // 已退款
}

// 支付方式枚举
export enum PaymentMethod {
  ALIPAY = 'ALIPAY',           // 支付宝
  WECHAT = 'WECHAT',           // 微信支付
  CASH = 'CASH',               // 货到付款
  CREDIT_CARD = 'CREDIT_CARD'  // 信用卡
}

// 订单项类型
export interface OrderItem {
  orderItemId: number
  orderId: number
  productId: number
  quantity: number
  unitPrice: number
  subtotal: number
  productNameSnapshot?: string
  brandSnapshot?: string
  categorySnapshot?: string
  imageUrlSnapshot?: string
  reviewed?: boolean
}

// 订单类型
export interface Order {
  orderId: number
  userId: number
  orderNumber: string
  totalAmount: number
  orderStatus: OrderStatus
  paymentMethod: PaymentMethod
  orderDate: string
  shippingAddress: string
  contactPhone: string
  remark?: string
  deletedAt?: string
}

// 订单详情类型（包含订单项）
export interface OrderDetail {
  order: Order
  orderItems: OrderItem[]
}

export interface CheckoutItemPayload {
  cartItemId?: number
  productId: number
  quantity: number
}

// 创建订单请求类型
export interface CreateOrderRequest {
  shippingAddress: string
  contactPhone: string
  paymentMethod: string
  remark?: string
  items?: CheckoutItemPayload[]
}

// 订单统计类型
export interface OrderStats {
  totalOrders: number
  pendingOrders: number
  paidOrders: number
  shippedOrders: number
  deliveredOrders: number
  cancelledOrders: number
  totalAmount: number
}

// 订单API
export const orderApi = {
  // 从购物车创建订单
  createOrderFromCart(data: CreateOrderRequest) {
    return request.post<Order>('/order/create', data, { showError: false })
  },

  // 获取用户订单列表
  getUserOrders(page: number = 1, size: number = 10, status?: string) {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString()
    })
    if (status) {
      params.append('status', status)
    }
    return request.get<Order[]>(`/order/list?${params.toString()}`)
  },

  // 获取订单详情
  getOrderDetail(orderId: number) {
    return request.get<OrderDetail>(`/order/detail/${orderId}`)
  },

  // 取消订单
  cancelOrder(orderId: number) {
    return request.put(`/order/cancel/${orderId}`)
  },

  // 确认收货
  confirmOrder(orderId: number) {
    return request.put(`/order/confirm/${orderId}`)
  },

  // 删除订单（软删除）
  deleteOrder(orderId: number) {
    return request.delete(`/order/delete/${orderId}`)
  },

  // 模拟支付
  payOrder(orderId: number) {
    return request.put(`/order/pay/${orderId}`)
  },

  // 获取订单统计信息
  getOrderStats() {
    return request.get<OrderStats>('/order/stats')
  }
} 
