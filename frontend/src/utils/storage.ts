/**
 * 本地存储工具类
 * 支持localStorage和sessionStorage的统一操作
 */

export enum StorageType {
  Local = 'localStorage',
  Session = 'sessionStorage'
}

class StorageManager {
  private storage: globalThis.Storage

  constructor(type: StorageType = StorageType.Local) {
    this.storage = type === StorageType.Local ? globalThis.localStorage : globalThis.sessionStorage
  }

  /**
   * 设置存储项
   */
  set<T>(key: string, value: T): void {
    try {
      const serializedValue = JSON.stringify(value)
      this.storage.setItem(key, serializedValue)
    } catch (error) {
      console.error('Error setting storage item:', error)
    }
  }

  /**
   * 获取存储项
   */
  get<T>(key: string): T | null {
    try {
      const item = this.storage.getItem(key)
      if (item === null) return null
      return JSON.parse(item) as T
    } catch (error) {
      console.error('Error getting storage item:', error)
      return null
    }
  }

  /**
   * 移除存储项
   */
  remove(key: string): void {
    this.storage.removeItem(key)
  }

  /**
   * 清空所有存储项
   */
  clear(): void {
    this.storage.clear()
  }

  /**
   * 检查存储项是否存在
   */
  has(key: string): boolean {
    return this.storage.getItem(key) !== null
  }

  /**
   * 获取所有键名
   */
  keys(): string[] {
    const keys: string[] = []
    for (let i = 0; i < this.storage.length; i++) {
      const key = this.storage.key(i)
      if (key) keys.push(key)
    }
    return keys
  }

  /**
   * 获取存储大小（字节）
   */
  size(): number {
    let total = 0
    for (let i = 0; i < this.storage.length; i++) {
      const key = this.storage.key(i)
      if (key) {
        const value = this.storage.getItem(key)
        if (value) {
          total += key.length + value.length
        }
      }
    }
    return total
  }
}

// 默认实例
export const localStorageManager = new StorageManager(StorageType.Local)
export const sessionStorageManager = new StorageManager(StorageType.Session)

// 特定业务存储键名常量
export const STORAGE_KEYS = {
  TOKEN: 'tech_parts_token',
  USER_INFO: 'tech_parts_user_info',
  CART_ITEMS: 'tech_parts_cart_items',
  SEARCH_HISTORY: 'tech_parts_search_history',
  THEME: 'tech_parts_theme',
  LANGUAGE: 'tech_parts_language'
} as const

// 用户相关存储操作
export const userStorage = {
  // 设置用户token
  setToken(token: string): void {
    localStorageManager.set(STORAGE_KEYS.TOKEN, token)
  },

  // 获取用户token
  getToken(): string | null {
    return localStorageManager.get<string>(STORAGE_KEYS.TOKEN)
  },

  // 移除用户token
  removeToken(): void {
    localStorageManager.remove(STORAGE_KEYS.TOKEN)
  },

  // 设置用户信息
  setUserInfo(userInfo: any): void {
    localStorageManager.set(STORAGE_KEYS.USER_INFO, userInfo)
  },

  // 获取用户信息
  getUserInfo<T>(): T | null {
    return localStorageManager.get<T>(STORAGE_KEYS.USER_INFO)
  },

  // 移除用户信息
  removeUserInfo(): void {
    localStorageManager.remove(STORAGE_KEYS.USER_INFO)
  },

  // 清除所有用户数据
  clearUserData(): void {
    localStorageManager.remove(STORAGE_KEYS.TOKEN)
    localStorageManager.remove(STORAGE_KEYS.USER_INFO)
  }
}

// 购物车相关存储操作
export const cartStorage = {
  // 设置购物车商品
  setCartItems(items: any[]): void {
    localStorageManager.set(STORAGE_KEYS.CART_ITEMS, items)
  },

  // 获取购物车商品
  getCartItems<T = any>(): T[] {
    return localStorageManager.get<T[]>(STORAGE_KEYS.CART_ITEMS) || []
  },

  // 添加商品到购物车
  addCartItem(item: any): void {
    const items = this.getCartItems()
    const existingIndex = items.findIndex((i: any) => i.id === item.id)
    
    if (existingIndex > -1) {
      items[existingIndex].quantity += item.quantity || 1
    } else {
      items.push({ ...item, quantity: item.quantity || 1 })
    }
    
    this.setCartItems(items)
  },

  // 移除购物车商品
  removeCartItem(itemId: number | string): void {
    const items = this.getCartItems()
    const filteredItems = items.filter((item: any) => item.id !== itemId)
    this.setCartItems(filteredItems)
  },

  // 更新商品数量
  updateCartItemQuantity(itemId: number | string, quantity: number): void {
    const items = this.getCartItems()
    const itemIndex = items.findIndex((item: any) => item.id === itemId)
    
    if (itemIndex > -1) {
      if (quantity <= 0) {
        items.splice(itemIndex, 1)
      } else {
        items[itemIndex].quantity = quantity
      }
      this.setCartItems(items)
    }
  },

  // 清空购物车
  clearCart(): void {
    localStorageManager.remove(STORAGE_KEYS.CART_ITEMS)
  },

  // 获取购物车商品数量
  getCartItemCount(): number {
    const items = this.getCartItems()
    return items.reduce((total: number, item: any) => total + (item.quantity || 0), 0)
  }
} 
