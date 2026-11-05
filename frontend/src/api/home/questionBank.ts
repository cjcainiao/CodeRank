// 首页题库接口
import requestUtil from '@/utils/requestUtil'

// 题库列表项
export interface QuestionListItemVO {
  id: string
  title: string
  difficulty: number
  judgeMode: number
  submitCount: number
  acceptedCount: number
  tags: string[] | null
}

// 分页结果
export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

// 题库查询参数
export interface QuestionListQuery {
  pageNum?: number
  pageSize?: number
  title?: string
  difficulty?: number
  tagId?: string
  sortField?: string
  sortOrder?: 'asc' | 'desc'
}

// 标签信息
export interface TagOption {
  id: string
  name: string
  color: string | null
}

// 语言初始代码
export interface CodeTemplate {
  language: number
  template: string
}

// 题目详情
export interface QuestionDetailVO {
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
  submitCount: number
  acceptedCount: number
  tags: string[] | null
  codeTemplates: CodeTemplate[] | null
}

// 分页查询已发布的题目
export function pageQuestionBank(query: QuestionListQuery): Promise<PageResult<QuestionListItemVO>> {
  return requestUtil.get<PageResult<QuestionListItemVO>>('/home/questions', { params: query })
}

// 查询可筛选的标签
export function listTagOptions(): Promise<TagOption[]> {
  return requestUtil.get<TagOption[]>('/home/questions/tags')
}

// 按 id 查询题目详情
export function getQuestionDetail(id: string): Promise<QuestionDetailVO> {
  return requestUtil.get<QuestionDetailVO>(`/home/questions/${id}`)
}
