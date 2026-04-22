import request from './request'

export interface ProductReview {
  reviewId?: number
  orderId?: number
  orderItemId: number
  userId?: number
  productId?: number
  rating: number
  content?: string
  createdAt?: string
  updatedAt?: string
  username?: string
  avatarUrl?: string
  productNameSnapshot?: string
  imageUrlSnapshot?: string
}

export interface ProductReviewSummary {
  averageRating: number
  reviewCount: number
}

export const reviewApi = {
  getProductReviews(productId: number) {
    return request.get<ProductReview[]>(`/review/product/${productId}`)
  },

  getProductReviewSummary(productId: number) {
    return request.get<ProductReviewSummary>(`/review/summary/${productId}`)
  },

  canReview(orderItemId: number) {
    return request.get<{ canReview: boolean }>(`/review/can-review/${orderItemId}`)
  },

  getReviewableOrderItem(productId: number) {
    return request.get<{ canReview: boolean; orderItem?: { orderItemId: number; orderId: number; productId: number } }>(
      `/review/product/${productId}/reviewable`,
      { showError: false }
    )
  },

  submitReview(payload: ProductReview) {
    return request.post('/review/submit', payload)
  }
}
