// 后台题目管理接口
import requestUtil from '@/utils/requestUtil'

// 题目信息
export interface QuestionVO {
  id: string
  title: string
  difficulty: number
  judgeMode: number
  description: string
  inputFormat: string | null
  outputFormat: string | null
  sampleCases: string | null
  hint: string | null
  timeLimit: number
  memoryLimit: number
  source: string | null
  status: number
  submitCount: number
  acceptedCount: number
  tagIds: string[] | null
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

// 题目分页查询参数
export interface QuestionQuery {
  pageNum?: number
  pageSize?: number
  title?: string
  difficulty?: number
  judgeMode?: number
  status?: number
  source?: string
  sortField?: string
  sortOrder?: 'asc' | 'desc'
}

// 新增题目参数
export interface QuestionCreatePayload {
  title: string
  difficulty?: number
  judgeMode?: number
  description: string
  inputFormat?: string
  outputFormat?: string
  sampleCases?: string
  hint?: string
  timeLimit?: number
  memoryLimit?: number
  source?: string
  status?: number
  tagIds?: string[]
}

// 修改题目参数
export interface QuestionUpdatePayload {
  id: string
  title?: string
  difficulty?: number
  judgeMode?: number
  description?: string
  inputFormat?: string
  outputFormat?: string
  sampleCases?: string
  hint?: string
  timeLimit?: number
  memoryLimit?: number
  source?: string
  status?: number
  tagIds?: string[]
}

// 分页查询题目
export function pageQuestions(query: QuestionQuery): Promise<PageResult<QuestionVO>> {
  return requestUtil.get<PageResult<QuestionVO>>('/backend/questions', { params: query })
}

// 按 id 查询题目
export function getQuestionById(id: string): Promise<QuestionVO> {
  return requestUtil.get<QuestionVO>(`/backend/questions/${id}`)
}

// 新增题目，返回新题目主键
export function createQuestion(payload: QuestionCreatePayload): Promise<string> {
  return requestUtil.post<string, QuestionCreatePayload>('/backend/questions', payload)
}

// 修改题目
export function updateQuestion(payload: QuestionUpdatePayload): Promise<void> {
  return requestUtil.put<void, QuestionUpdatePayload>('/backend/questions', payload)
}

// 删除题目
export function deleteQuestion(id: string): Promise<void> {
  return requestUtil.delete<void>(`/backend/questions/${id}`)
}
