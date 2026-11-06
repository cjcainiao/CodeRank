// 做题页代码运行接口
import requestUtil from '@/utils/requestUtil'

// 运行结果
export interface RunResultVO {
  stage: string
  status: string | null
  exitStatus: number | null
  time: number | null
  memory: number | null
  stdout: string | null
  stderr: string | null
  error: string | null
}

// 运行参数
export interface RunCodePayload {
  questionId: string
  language: string
  code: string
  input?: string
}

// 运行一次代码，结果不落库
export function runCode(payload: RunCodePayload): Promise<RunResultVO> {
  return requestUtil.post<RunResultVO, RunCodePayload>('/problem/run', payload)
}
