// 后台题目评测数据接口
import requestUtil from '@/utils/requestUtil'

// 评测数据信息
export interface QuestionTestDataVO {
  id: string
  questionId: string
  caseNo: number
  inputSize: number
  outputSize: number
  visible: number
  createTime: string
  updateTime: string
}

// 查询题目下的全部评测数据
export function listQuestionTestData(questionId: string): Promise<QuestionTestDataVO[]> {
  return requestUtil.get<QuestionTestDataVO[]>(`/backend/question-test-data/question/${questionId}`)
}

// 上传一组评测数据，返回主键
export function uploadQuestionTestData(
  questionId: string,
  inputFile: File,
  outputFile: File,
  visible?: number,
): Promise<string> {
  const formData = new FormData()
  formData.append('inputFile', inputFile)
  formData.append('outputFile', outputFile)
  if (visible !== undefined) formData.append('visible', String(visible))
  return requestUtil.post<string, FormData>(`/backend/question-test-data/question/${questionId}/upload`, formData)
}

// 修改评测数据参数
export interface QuestionTestDataUpdatePayload {
  id: string
  caseNo?: number
  inputSize?: number
  outputSize?: number
  visible?: number
}

// 修改评测数据
export function updateQuestionTestData(payload: QuestionTestDataUpdatePayload): Promise<void> {
  return requestUtil.put<void, QuestionTestDataUpdatePayload>(
    '/backend/question-test-data',
    payload,
  )
}

// 删除评测数据
export function deleteQuestionTestData(id: string): Promise<void> {
  return requestUtil.delete<void>(`/backend/question-test-data/${id}`)
}
