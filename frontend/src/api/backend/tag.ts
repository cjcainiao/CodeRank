// 后台标签管理接口
import requestUtil from '@/utils/requestUtil'

// 标签信息
export interface TagVO {
  id: string
  name: string
  color: string | null
  sortOrder: number
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

// 标签分页查询参数
export interface TagQuery {
  pageNum?: number
  pageSize?: number
  name?: string
  sortField?: string
  sortOrder?: 'asc' | 'desc'
}

// 新增标签参数
export interface TagCreatePayload {
  name: string
  color?: string
  sortOrder?: number
}

// 修改标签参数
export interface TagUpdatePayload {
  id: string
  name?: string
  color?: string
  sortOrder?: number
}

// 分页查询标签
export function pageTags(query: TagQuery): Promise<PageResult<TagVO>> {
  return requestUtil.get<PageResult<TagVO>>('/backend/tags', { params: query })
}

// 按 id 查询标签
export function getTagById(id: string): Promise<TagVO> {
  return requestUtil.get<TagVO>(`/backend/tags/${id}`)
}

// 新增标签，返回新标签主键
export function createTag(payload: TagCreatePayload): Promise<string> {
  return requestUtil.post<string, TagCreatePayload>('/backend/tags', payload)
}

// 修改标签
export function updateTag(payload: TagUpdatePayload): Promise<void> {
  return requestUtil.put<void, TagUpdatePayload>('/backend/tags', payload)
}

// 删除标签
export function deleteTag(id: string): Promise<void> {
  return requestUtil.delete<void>(`/backend/tags/${id}`)
}
