// 后台评测机指标接口
import requestUtil from '@/utils/requestUtil'

// 单条指标
export interface JudgeMetricVO {
  name: string
  help: string | null
  labels: Record<string, string>
  value: number | null
}

// 指标快照
export interface JudgeMetricsVO {
  machineId: string
  machineName: string
  endpoint: string
  available: boolean
  elapsed: number
  message: string | null
  metrics: JudgeMetricVO[] | null
}

// 抓取评测机运行指标
export function getJudgeMetrics(id: string): Promise<JudgeMetricsVO> {
  return requestUtil.get<JudgeMetricsVO>(`/backend/judge-machines/${id}/metrics`)
}
