import axios from 'axios'
import type { AxiosError, AxiosInstance, AxiosRequestConfig } from 'axios'
import { ElLoading, ElMessage } from 'element-plus'
import { userStorage } from '@/utils/storage'

interface RequestConfig extends AxiosRequestConfig {
  showLoading?: boolean
  showError?: boolean
  retry?: number
  retryDelay?: number
}

interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp?: string
}

class LoadingManager {
  private loadingInstance: any = null
  private requestCount = 0

  show(text = '加载中...') {
    if (this.requestCount === 0) {
      this.loadingInstance = ElLoading.service({
        text,
        background: 'rgba(0, 0, 0, 0.3)',
        lock: true
      })
    }
    this.requestCount += 1
  }

  hide() {
    this.requestCount -= 1
    if (this.requestCount <= 0) {
      this.requestCount = 0
      if (this.loadingInstance) {
        this.loadingInstance.close()
        this.loadingInstance = null
      }
    }
  }

  forceHide() {
    this.requestCount = 0
    if (this.loadingInstance) {
      this.loadingInstance.close()
      this.loadingInstance = null
    }
  }
}

const loadingManager = new LoadingManager()
const apiBaseURL = (import.meta.env.VITE_API_BASE_URL || '/api').trim()
const appBasePath = (import.meta.env.BASE_URL || '/').replace(/\/+$/, '')

const normalizeConfig = (config?: RequestConfig) => ({
  showLoading: config?.showLoading === true,
  showError: config?.showError !== false
})

const extractMessage = (payload: any, fallback: string) => {
  if (typeof payload?.message === 'string' && payload.message.trim()) {
    return payload.message.trim()
  }
  return fallback
}

const buildHttpErrorMessage = (status?: number, data?: any) => {
  switch (status) {
    case 400:
      return extractMessage(data, '请求参数错误')
    case 401:
      return '登录已过期，请重新登录'
    case 403:
      return extractMessage(data, '没有权限访问')
    case 404:
      return '请求的资源不存在'
    case 422:
      return extractMessage(data, '数据验证失败')
    case 500:
      return '服务器内部错误'
    case 502:
      return '网关错误'
    case 503:
      return '服务暂不可用'
    case 504:
      return '网关超时'
    default:
      return status ? `请求失败 (${status})` : '请求失败'
  }
}

const toRequestError = (message: string, extra: Record<string, any> = {}) => {
  const requestError = new Error(message) as Error & Record<string, any>
  Object.assign(requestError, extra)
  return requestError
}

const handleUnauthorized = async () => {
  loadingManager.forceHide()

  userStorage.clearUserData()

  const currentPath = `${window.location.pathname}${window.location.search}${window.location.hash}`
  const loginPath = `${appBasePath}/login`.replace(/\/{2,}/g, '/')
  const isLoginPage = window.location.pathname === loginPath

  if (!isLoginPage) {
    const redirect = encodeURIComponent(currentPath)
    window.location.replace(`${loginPath}?redirect=${redirect}`)
  }
}

const retryRequest = (
  instance: AxiosInstance,
  config: RequestConfig,
  retryCount = 0
): Promise<any> => {
  const maxRetries = config.retry || 3
  const retryDelay = config.retryDelay || 1000

  if (retryCount >= maxRetries) {
    return Promise.reject(toRequestError('请求失败，已达到最大重试次数'))
  }

  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(instance.request(config))
    }, retryDelay)
  }).catch(() => retryRequest(instance, config, retryCount + 1))
}

const request: AxiosInstance = axios.create({
  baseURL: apiBaseURL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

const fileRequest: AxiosInstance = axios.create({
  baseURL: apiBaseURL,
  timeout: 30000
})

request.interceptors.request.use(
  (config: any) => {
    const token = userStorage.getToken()
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }

    const normalized = normalizeConfig(config)
    if (normalized.showLoading) {
      loadingManager.show()
    }

    config.metadata = { startTime: Date.now() }
    return config
  },
  (error) => {
    loadingManager.hide()
    return Promise.reject(error)
  }
)

fileRequest.interceptors.request.use(
  (config: any) => {
    const token = userStorage.getToken()
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }

    const normalized = normalizeConfig(config)
    if (normalized.showLoading) {
      loadingManager.show('正在导出...')
    }
    return config
  },
  (error) => {
    loadingManager.hide()
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  async (response: any) => {
    const config = response.config as RequestConfig
    const { showLoading, showError } = normalizeConfig(config)

    if (showLoading) {
      loadingManager.hide()
    }

    const { data } = response

    if (data.code === 200) {
      return data
    }

    if (data.code === 401) {
      if (showError) {
        ElMessage.error('登录已过期，请重新登录')
      }
      await handleUnauthorized()
      return Promise.reject(toRequestError('登录已过期，请重新登录', { code: 401, response }))
    }

    if (data.code === 403) {
      const message = '没有权限访问该资源'
      if (showError) {
        ElMessage.error(message)
      }
      return Promise.reject(toRequestError(message, { code: 403 }))
    }

    const message = extractMessage(data, '请求失败')
    if (showError) {
      ElMessage.error(message)
    }
    return Promise.reject(toRequestError(message, { code: data.code, response }))
  },
  async (error: AxiosError) => {
    const config = (error.config || {}) as RequestConfig
    const { showLoading, showError } = normalizeConfig(config)

    if (showLoading) {
      loadingManager.hide()
    }

    const isTimeout = error.code === 'ECONNABORTED' || error.message?.includes('timeout')
    if (isTimeout && config.retry && config.retry > 0) {
      return retryRequest(request, config)
    }

    if (error.response) {
      const message = buildHttpErrorMessage(error.response.status, error.response.data)
      if (error.response.status === 401) {
        if (showError) {
          ElMessage.error(message)
        }
        await handleUnauthorized()
      } else if (showError) {
        ElMessage.error(message)
      }

      return Promise.reject(
        toRequestError(message, {
          status: error.response.status,
          response: error.response
        })
      )
    }

    if (error.request) {
      const message = '网络连接失败，请检查网络设置'
      if (showError) {
        ElMessage.error(message)
      }
      return Promise.reject(toRequestError(message, { request: error.request }))
    }

    const message = error.message || '未知错误'
    if (showError) {
      ElMessage.error(message)
    }
    return Promise.reject(toRequestError(message))
  }
)

fileRequest.interceptors.response.use(
  (response) => {
    const config = response.config as RequestConfig
    const { showLoading } = normalizeConfig(config)
    if (showLoading) {
      loadingManager.hide()
    }
    return response
  },
  async (error: AxiosError) => {
    const config = (error.config || {}) as RequestConfig
    const { showLoading, showError } = normalizeConfig(config)
    if (showLoading) {
      loadingManager.hide()
    }

    if (error.response) {
      const message = buildHttpErrorMessage(error.response.status, error.response.data)
      if (error.response.status === 401) {
        if (showError) {
          ElMessage.error(message)
        }
        await handleUnauthorized()
      } else if (showError) {
        ElMessage.error(message)
      }
      return Promise.reject(
        toRequestError(message, {
          status: error.response.status,
          response: error.response
        })
      )
    }

    const message = error.message || '下载失败'
    if (showError) {
      ElMessage.error(message)
    }
    return Promise.reject(toRequestError(message))
  }
)

export const api = {
  get<T = any>(url: string, config?: RequestConfig): Promise<ApiResponse<T>> {
    return request.get(url, config)
  },

  post<T = any>(url: string, data?: any, config?: RequestConfig): Promise<ApiResponse<T>> {
    return request.post(url, data, config)
  },

  put<T = any>(url: string, data?: any, config?: RequestConfig): Promise<ApiResponse<T>> {
    return request.put(url, data, config)
  },

  delete<T = any>(url: string, config?: RequestConfig): Promise<ApiResponse<T>> {
    return request.delete(url, config)
  },

  patch<T = any>(url: string, data?: any, config?: RequestConfig): Promise<ApiResponse<T>> {
    return request.patch(url, data, config)
  },

  upload<T = any>(url: string, file: File, config?: RequestConfig): Promise<ApiResponse<T>> {
    const formData = new FormData()
    formData.append('file', file)

    return request.post(url, formData, {
      ...config,
      headers: {
        'Content-Type': 'multipart/form-data',
        ...config?.headers
      }
    })
  },

  download(url: string, config?: RequestConfig) {
    return fileRequest.get<Blob>(url, {
      ...config,
      responseType: 'blob'
    })
  },

  all<T = any>(requests: Promise<any>[]): Promise<T[]> {
    return Promise.all(requests)
  },

  cancelAllRequests() {
    loadingManager.forceHide()
  }
}

export default request
export type { ApiResponse, RequestConfig }
