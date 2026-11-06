// 后台题目代码模板与测试用例接口
import requestUtil from '@/utils/requestUtil'

// 代码模板信息
export interface QuestionCodeTemplateVO {
  id: string
  questionId: string
  language: number
  template: string
  stubTemplate: string | null
  createTime: string
  updateTime: string
}

// 新增代码模板参数
export interface QuestionCodeTemplateCreatePayload {
  questionId: string
  language: number
  template: string
  stubTemplate?: string
}

// 修改代码模板参数
export interface QuestionCodeTemplateUpdatePayload {
  id: string
  language?: number
  template?: string
  stubTemplate?: string
}

// 测试用例信息
export interface QuestionTestCaseVO {
  id: string
  questionId: string
  args: string | null
  expected: string | null
  inputData: string | null
  expectedOut: string | null
  visible: number
  sortOrder: number
  createTime: string
  updateTime: string
}

// 新增测试用例参数
export interface QuestionTestCaseCreatePayload {
  questionId: string
  args?: string
  expected?: string
  inputData?: string
  expectedOut?: string
  visible?: number
  sortOrder?: number
}

// 修改测试用例参数
export interface QuestionTestCaseUpdatePayload {
  id: string
  args?: string
  expected?: string
  inputData?: string
  expectedOut?: string
  visible?: number
  sortOrder?: number
}

// 查询题目下的全部代码模板
export function listQuestionCodeTemplates(questionId: string): Promise<QuestionCodeTemplateVO[]> {
  return requestUtil.get<QuestionCodeTemplateVO[]>(
    `/backend/question-code-templates/question/${questionId}`,
  )
}

// 新增代码模板，返回主键
export function createQuestionCodeTemplate(
  payload: QuestionCodeTemplateCreatePayload,
): Promise<string> {
  return requestUtil.post<string, QuestionCodeTemplateCreatePayload>(
    '/backend/question-code-templates',
    payload,
  )
}

// 修改代码模板
export function updateQuestionCodeTemplate(
  payload: QuestionCodeTemplateUpdatePayload,
): Promise<void> {
  return requestUtil.put<void, QuestionCodeTemplateUpdatePayload>(
    '/backend/question-code-templates',
    payload,
  )
}

// 删除代码模板
export function deleteQuestionCodeTemplate(id: string): Promise<void> {
  return requestUtil.delete<void>(`/backend/question-code-templates/${id}`)
}

// 查询题目下的全部测试用例
export function listQuestionTestCases(questionId: string): Promise<QuestionTestCaseVO[]> {
  return requestUtil.get<QuestionTestCaseVO[]>(`/backend/question-test-cases/question/${questionId}`)
}

// 新增测试用例，返回主键
export function createQuestionTestCase(payload: QuestionTestCaseCreatePayload): Promise<string> {
  return requestUtil.post<string, QuestionTestCaseCreatePayload>(
    '/backend/question-test-cases',
    payload,
  )
}

// 修改测试用例
export function updateQuestionTestCase(payload: QuestionTestCaseUpdatePayload): Promise<void> {
  return requestUtil.put<void, QuestionTestCaseUpdatePayload>(
    '/backend/question-test-cases',
    payload,
  )
}

// 删除测试用例
export function deleteQuestionTestCase(id: string): Promise<void> {
  return requestUtil.delete<void>(`/backend/question-test-cases/${id}`)
}
