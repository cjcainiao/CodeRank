// 后台用户管理接口
import requestUtil from '@/utils/requestUtil'

// 用户信息
export interface UserVO {
  id: string
  username: string
  nickname: string | null
  email: string | null
  phone: string | null
  avatar: string | null
  signature: string | null
  gender: number
  role: number
  status: number
  lastLoginTime: string | null
  createTime: string
  updateTime: string
}

// 分页结果
export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

// 用户分页查询参数
export interface UserQuery {
  pageNum?: number
  pageSize?: number
  username?: string
  nickname?: string
  status?: number
  role?: number
  sortField?: string
  sortOrder?: 'asc' | 'desc'
}

// 新增用户参数
export interface UserCreatePayload {
  username: string
  password: string
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  signature?: string
  gender?: number
  role?: number
}

// 修改用户参数
export interface UserUpdatePayload {
  id: string
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  signature?: string
  gender?: number
  role?: number
  status?: number
}

// 分页查询用户
export function pageUsers(query: UserQuery): Promise<PageResult<UserVO>> {
  return requestUtil.get<PageResult<UserVO>>('/backend/users', { params: query })
}

// 按 id 查询用户
export function getUserById(id: string): Promise<UserVO> {
  return requestUtil.get<UserVO>(`/backend/users/${id}`)
}

// 新增用户，返回新用户主键
export function createUser(payload: UserCreatePayload): Promise<string> {
  return requestUtil.post<string, UserCreatePayload>('/backend/users', payload)
}

// 修改用户
export function updateUser(payload: UserUpdatePayload): Promise<void> {
  return requestUtil.put<void, UserUpdatePayload>('/backend/users', payload)
}

// 删除用户
export function deleteUser(id: string): Promise<void> {
  return requestUtil.delete<void>(`/backend/users/${id}`)
}
