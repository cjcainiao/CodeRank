// 后台提交记录接口
import requestUtil from '@/utils/requestUtil'

// 提交记录
export interface SubmissionVO {
  id: string
  questionId: string
  questionTitle: string | null
  language: string
  status: number
  passedCount: number
  totalCount: number
  timeUsed: number
  memoryUsed: number
  errorMessage: string | null
  judgeMachineId: string | null
  sourceCode: string
  createTime: string
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
export interface SubmissionQuery {
  pageNum?: number
  pageSize?: number
  questionId?: string
  language?: string
  status?: number
  sortField?: string
  sortOrder?: 'asc' | 'desc'
}

// 分页查询提交记录
export function pageSubmissions(query: SubmissionQuery): Promise<PageResult<SubmissionVO>> {
  return requestUtil.get<PageResult<SubmissionVO>>('/backend/submissions', { params: query })
}
