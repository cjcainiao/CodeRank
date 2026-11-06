// 做题页提交评测接口
import requestUtil from '@/utils/requestUtil'

// 单个测试点结果
export interface JudgeCaseVO {
  caseNo: number
  passed: boolean
  status: string | null
  exitStatus: number | null
  time: number | null
  memory: number | null
  output: string | null
  message: string | null
}

// 提交评测任务状态
export interface SubmissionStatusVO {
  submissionId: string
  status: number
  finished: boolean
  result: string | null
  accepted: boolean | null
  passedCount: number | null
  totalCount: number | null
  timeUsed: number | null
  memoryUsed: number | null
  message: string | null
  cases: JudgeCaseVO[] | null
}

// 提交参数
export interface SubmitCodePayload {
  questionId: string
  language: string
  code: string
}

// 提交代码，返回评测任务 id
export function submitCode(payload: SubmitCodePayload): Promise<string> {
  return requestUtil.post<string, SubmitCodePayload>('/problem/submit', payload)
}

// 查询评测任务状态
export function getSubmissionStatus(submissionId: string): Promise<SubmissionStatusVO> {
  return requestUtil.get<SubmissionStatusVO>(`/problem/submit/${submissionId}`)
}
