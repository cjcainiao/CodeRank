// Axios 请求和错误处理
import axios, {
  type AxiosError,
  type AxiosInstance,
  type AxiosRequestConfig,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios'
import { ElMessage } from 'element-plus'

const AUTH_TOKEN_STORAGE_KEY = 'coderank-auth-token'

// 成功状态码
const CODE_SUCCESS = 200

// 后端统一响应结构
export interface ApiResult<T = unknown> {
  code: number
  msg: string
  data: T | null
}

// 提取接口返回的错误消息
function responseMessage(data: unknown): string | undefined {
  if (typeof data === 'string') {
    const message = data.trim()
    return message && !message.startsWith('<') ? message : undefined
  }
  if (!data || typeof data !== 'object') return undefined

  const payload = data as Record<string, unknown>
  for (const field of ['msg', 'message', 'detail']) {
    const value = payload[field]
    if (typeof value === 'string' && value.trim()) return value.trim()
  }
  return undefined
}

// 统一的请求错误，保留状态码与原始响应便于调用方判断
export class RequestError extends Error {
  readonly status?: number
  readonly code?: number | string
  readonly data?: unknown
  readonly originalError?: AxiosError

  constructor(
    message: string,
    options: {
      status?: number
      code?: number | string
      data?: unknown
      originalError?: AxiosError
    } = {},
  ) {
    super(message)
    this.name = 'RequestError'
    this.status = options.status
    this.code = options.code
    this.data = options.data
    this.originalError = options.originalError
  }
}

// 请求配置扩展
export interface RequestConfig<D = unknown> extends AxiosRequestConfig<D> {
  // 跳过携带登录令牌
  skipAuth?: boolean
  // 登录接口专用，成功后保存响应头里的令牌
  persistAuthToken?: boolean
  // 跳过自动错误提示，由调用方自行处理
  skipErrorMessage?: boolean
}

export class RequestUtil {
  private readonly client: AxiosInstance

  constructor() {
    this.client = axios.create({
      // 开发环境走 Vite 代理的 /api 前缀，跨源部署才配 VITE_API_BASE_URL
      baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
      timeout: 30_000,
      headers: {
        Accept: 'application/json',
      },
    })

    this.setupInterceptors()
  }

  // 注册请求和响应拦截器
  private setupInterceptors(): void {
    this.client.interceptors.request.use(
      (config) => this.beforeRequest(config),
      (error: AxiosError) => Promise.reject(error),
    )

    this.client.interceptors.response.use(
      (response) => this.afterResponse(response),
      (error: unknown) => Promise.reject(this.handleError(error)),
    )
  }

  // 响应成功时保存令牌，业务码失败时转成请求错误
  private afterResponse(response: AxiosResponse): AxiosResponse | Promise<never> {
    this.saveAuthToken(response)

    const result = response.data as ApiResult<unknown> | null
    // 非统一响应结构（如文件流）直接放行，交给调用方处理
    if (!result || typeof result !== 'object' || typeof result.code !== 'number') return response

    if (result.code === CODE_SUCCESS) return response

    const message = result.msg || '请求失败'
    this.notifyError(response.config as RequestConfig, message)
    return Promise.reject(new RequestError(message, { code: result.code, data: result.data }))
  }

  // 归一化错误，并提示一次
  private handleError(error: unknown): Error {
    const normalized = this.normalizeError(error)

    // 登录过期时清空令牌，避免界面残留登录态
    if (normalized instanceof RequestError && normalized.status === 401) {
      this.clearAuthToken()
    }

    const config = axios.isAxiosError(error)
      ? (error.config as RequestConfig | undefined)
      : undefined
    this.notifyError(config, normalized.message)
    return normalized
  }

  // 按配置决定是否弹出错误提示
  private notifyError(config: RequestConfig | undefined, message: string): void {
    if (config?.skipErrorMessage) return
    ElMessage.error(message)
  }

  // 请求前置处理
  private beforeRequest(config: InternalAxiosRequestConfig): InternalAxiosRequestConfig {
    // 普通 JSON 请求默认声明响应格式；FormData 交给浏览器自动生成 Content-Type
    if (!config.headers.has('Accept')) {
      config.headers.set('Accept', 'application/json')
    }

    const isFormData = typeof FormData !== 'undefined' && config.data instanceof FormData
    if (!isFormData && config.data !== undefined && !config.headers.has('Content-Type')) {
      config.headers.set('Content-Type', 'application/json')
    }

    const requestConfig = config as InternalAxiosRequestConfig & RequestConfig
    if (!requestConfig.skipAuth && !config.headers.has('Authorization')) {
      const token = this.getAuthToken()
      if (token) config.headers.set('Authorization', `Bearer ${token}`)
    }
    return config
  }

  // 每次请求前读取当前登录令牌
  getAuthToken(): string | null {
    if (typeof window === 'undefined') return null
    try {
      return window.localStorage.getItem(AUTH_TOKEN_STORAGE_KEY)
    } catch {
      return null
    }
  }

  // 清除失效的登录令牌
  clearAuthToken(): void {
    if (typeof window === 'undefined') return
    try {
      window.localStorage.removeItem(AUTH_TOKEN_STORAGE_KEY)
    } catch {
      // 浏览器禁用存储时仍允许清除内存中的用户信息。
    }
  }

  // 登录成功时保存响应头令牌
  private saveAuthToken(response: AxiosResponse): void {
    const requestConfig = response.config as InternalAxiosRequestConfig & RequestConfig
    const result = response.data as ApiResult<unknown> | null
    if (requestConfig.persistAuthToken && result?.code === CODE_SUCCESS && result.data) {
      const authorization = response.headers['authorization']
      const token =
        typeof authorization === 'string' ? /^Bearer\s+(\S+)$/i.exec(authorization)?.[1] : null
      if (!token) throw new Error('登录响应缺少令牌')
      try {
        window.localStorage.setItem(AUTH_TOKEN_STORAGE_KEY, token)
      } catch {
        throw new Error('登录凭证保存失败，请检查浏览器存储设置')
      }
    }
  }

  // 将 Axios 错误转换成业务层可识别的错误
  private normalizeError(error: unknown): Error {
    if (!axios.isAxiosError(error)) {
      return error instanceof Error ? error : new Error(responseMessage(error) || '请求失败')
    }

    const responseData = error.response?.data
    const message =
      responseMessage(responseData) ||
      (error.code === 'ECONNABORTED' ? '请求超时' : undefined) ||
      (!error.response
        ? error.code === 'ERR_NETWORK'
          ? '网络连接失败'
          : error.message || '网络连接失败'
        : `请求失败（HTTP ${error.response.status}）`)

    const code =
      responseData && typeof responseData === 'object' && 'code' in responseData
        ? (responseData as Record<string, unknown>).code
        : undefined
    const data =
      responseData && typeof responseData === 'object' && 'data' in responseData
        ? (responseData as Record<string, unknown>).data
        : responseData

    return new RequestError(message, {
      status: error.response?.status,
      code: typeof code === 'number' || typeof code === 'string' ? code : error.code,
      data,
      originalError: error,
    })
  }

  // 发起通用请求，返回响应体中的 data
  async request<T = unknown, D = unknown>(config: RequestConfig<D>): Promise<T> {
    const response = await this.client.request<ApiResult<T>, AxiosResponse<ApiResult<T>>, D>(config)
    return response.data.data as T
  }

  // GET 请求
  async get<T = unknown>(url: string, config?: RequestConfig): Promise<T> {
    const response = await this.client.get<ApiResult<T>>(url, config)
    return response.data.data as T
  }

  // POST 请求
  async post<T = unknown, D = unknown>(
    url: string,
    data?: D,
    config?: RequestConfig<D>,
  ): Promise<T> {
    const response = await this.client.post<ApiResult<T>>(url, data, config)
    return response.data.data as T
  }

  // PUT 请求
  async put<T = unknown, D = unknown>(
    url: string,
    data?: D,
    config?: RequestConfig<D>,
  ): Promise<T> {
    const response = await this.client.put<ApiResult<T>>(url, data, config)
    return response.data.data as T
  }

  // DELETE 请求
  async delete<T = unknown>(url: string, config?: RequestConfig): Promise<T> {
    const response = await this.client.delete<ApiResult<T>>(url, config)
    return response.data.data as T
  }

  // 暴露 Axios 实例，便于少数场景使用自定义能力
  getInstance(): AxiosInstance {
    return this.client
  }
}

export const requestUtil = new RequestUtil()

export default requestUtil
