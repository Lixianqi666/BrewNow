import request from './request'
import type { Product } from './product'

export const favoriteApi = {
  toggleFavorite(productId: number) {
    return request.post<{ favorited: boolean }>('/favorite/toggle', { productId })
  },

  getFavoriteStatus(productId: number) {
    return request.get<{ favorited: boolean }>(`/favorite/status?productId=${productId}`)
  },

  getFavorites() {
    return request.get<Product[]>('/favorite/list')
  }
}
