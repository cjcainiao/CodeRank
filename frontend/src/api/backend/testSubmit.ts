// 后台测试提交接口
import requestUtil from '@/utils/requestUtil'

// 测试提交结果
export interface TestSubmitResultVO {
  machineId: string
  machineName: string
  stage: string
  status: string | null
  exitStatus: number | null
  time: number | null
  memory: number | null
  stdout: string | null
  stderr: string | null
  error: string | null
}

// 测试提交参数
export interface TestSubmitPayload {
  machineId: string
  language: string
  code: string
  input?: string
}

// 提交代码到指定评测机运行一次
export function submitTestCode(payload: TestSubmitPayload): Promise<TestSubmitResultVO> {
  return requestUtil.post<TestSubmitResultVO, TestSubmitPayload>('/backend/test-submits', payload)
}
