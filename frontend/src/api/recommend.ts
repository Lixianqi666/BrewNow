import request, { api } from './request'
import type { Product } from './product'

export interface RecommendationItem {
  product: Product
  score: number
  strategy: string
  reason: string
}

export interface RecommendationMetrics {
  precisionAtK: number
  recallAtK: number
  hitRateAtK: number
  evaluatedUsers: number
}

export interface RecommendationEvaluation {
  topK: number
  lambda: number
  season: string
  baseline: RecommendationMetrics
  timeDecay: RecommendationMetrics
  seasonAware: RecommendationMetrics
}

export interface RecommendationStats {
  totalBehaviors: number
  activeUsers: number
  recommendableUsers: number
  activeProducts: number
  totalFavorites: number
  behaviorTypeCounts: Record<string, number>
  recentBehaviors: Array<{
    id: number
    userId: number
    productId: number
    behaviorType: string
    behaviorWeight: number
    createdAt: string
  }>
  evaluation: RecommendationEvaluation
}

export const recommendApi = {
  getHomeRecommendations(limit = 8) {
    return request.get<Product[]>(`/recommend/home?limit=${limit}`, { showLoading: false, showError: false })
  },

  getHomeRecommendationItems(limit = 8) {
    return request.get<RecommendationItem[]>(`/recommend/home/explain?limit=${limit}`, { showLoading: false, showError: false })
  },

  getRelatedProducts(productId: number, limit = 6) {
    return request.get<Product[]>(`/recommend/product/${productId}?limit=${limit}`)
  },

  getRelatedRecommendationItems(productId: number, limit = 6) {
    return request.get<RecommendationItem[]>(`/recommend/product/${productId}/explain?limit=${limit}`, { showError: false })
  },

  getRecommendationStats(topK = 10) {
    return request.get<RecommendationStats>(`/recommend/stats?topK=${topK}`, { showError: false })
  },

  getRecommendationEvaluation(topK = 10) {
    return request.get<RecommendationEvaluation>(`/recommend/evaluation?topK=${topK}`, { showError: false })
  },

  exportRecommendationStats(topK = 10) {
    return api.download(`/recommend/stats/export?topK=${topK}`, { showLoading: true })
  }
}
