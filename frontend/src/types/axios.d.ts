declare module 'axios' {
  export interface AxiosRequestConfig {
    showLoading?: boolean
    showError?: boolean
    retry?: number
    retryDelay?: number
    metadata?: {
      startTime?: number
    }
  }
}

export {}
