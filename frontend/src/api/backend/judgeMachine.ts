// 后台评测机接口
import requestUtil from '@/utils/requestUtil'

// 评测机视图对象
export interface JudgeMachineVO {
  id: string
  name: string
  endpoint: string
  hasToken: boolean
  status: number
  weight: number
  maxParallel: number
  cpuLimit: number
  clockLimit: number
  memoryLimit: number
  procLimit: number
  outputLimit: number
  lastCheckTime: string | null
  lastCheckOk: number
  remark: string | null
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

// 分页查询参数
export interface JudgeMachineQuery {
  pageNum?: number
  pageSize?: number
  name?: string
  status?: number
  sortField?: string
  sortOrder?: 'asc' | 'desc'
}

// 新增参数
export interface JudgeMachineCreatePayload {
  name: string
  endpoint: string
  authToken?: string
  status?: number
  weight?: number
  maxParallel?: number
  cpuLimit?: number
  clockLimit?: number
  memoryLimit?: number
  procLimit?: number
  outputLimit?: number
  remark?: string
}

// 修改参数
export interface JudgeMachineUpdatePayload extends JudgeMachineCreatePayload {
  id: string
}

// 探测结果
export interface JudgeMachineCheckVO {
  ok: boolean
  elapsed: number
  version: string | null
  os: string | null
  platform: string | null
  message: string | null
}

// 分页查询评测机
export function pageJudgeMachines(query: JudgeMachineQuery): Promise<PageResult<JudgeMachineVO>> {
  return requestUtil.get<PageResult<JudgeMachineVO>>('/backend/judge-machines', { params: query })
}

// 按 id 查询评测机
export function getJudgeMachine(id: string): Promise<JudgeMachineVO> {
  return requestUtil.get<JudgeMachineVO>(`/backend/judge-machines/${id}`)
}

// 新增评测机
export function createJudgeMachine(payload: JudgeMachineCreatePayload): Promise<string> {
  return requestUtil.post<string, JudgeMachineCreatePayload>('/backend/judge-machines', payload)
}

// 修改评测机
export function updateJudgeMachine(payload: JudgeMachineUpdatePayload): Promise<void> {
  return requestUtil.put<void, JudgeMachineUpdatePayload>('/backend/judge-machines', payload)
}

// 删除评测机
export function deleteJudgeMachine(id: string): Promise<void> {
  return requestUtil.delete<void>(`/backend/judge-machines/${id}`)
}

// 探测评测机是否可用
export function checkJudgeMachine(id: string): Promise<JudgeMachineCheckVO> {
  return requestUtil.post<JudgeMachineCheckVO, undefined>(
    `/backend/judge-machines/${id}/check`,
    undefined,
  )
}

// 探测全部评测机
export function checkAllJudgeMachines(): Promise<JudgeMachineCheckVO[]> {
  return requestUtil.post<JudgeMachineCheckVO[], undefined>(
    '/backend/judge-machines/check-all',
    undefined,
  )
}