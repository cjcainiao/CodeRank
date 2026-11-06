// 判题状态到展示文案、缩写与配色的映射

// 测试点方块的展示样式
export interface JudgeStatusStyle {
  // 状态缩写，如 AC、WA
  abbr: string
  // 状态全称
  label: string
  // 方块底色，采用判题站惯用的高饱和色系
  color: string
}

// 判题结果编号，与后端 submission.status 一致
export const JUDGE_STATUS = {
  PENDING: 0,
  ACCEPTED: 1,
  WRONG_ANSWER: 2,
  TIME_LIMIT: 3,
  MEMORY_LIMIT: 4,
  RUNTIME_ERROR: 5,
  COMPILE_ERROR: 6,
  SYSTEM_ERROR: 7,
  RUNNING: 8,
} as const

// 判题状态配色，复刻洛谷：AC 绿、WA 红、RE 紫、CE 黄，TLE/MLE 统一深灰
const COLOR_ACCEPTED = '#52c41a'
const COLOR_WRONG_ANSWER = '#e74c3c'
const COLOR_RUNTIME_ERROR = '#9d3dcf'
const COLOR_COMPILE_ERROR = '#f1c40f'
const COLOR_LIMIT_EXCEEDED = '#3f3f46'
const COLOR_NEUTRAL = '#bfbfbf'

// 各状态对应的缩写与配色
const STATUS_STYLES: Record<number, JudgeStatusStyle> = {
  [JUDGE_STATUS.ACCEPTED]: { abbr: 'AC', label: '通过', color: COLOR_ACCEPTED },
  [JUDGE_STATUS.WRONG_ANSWER]: { abbr: 'WA', label: '答案错误', color: COLOR_WRONG_ANSWER },
  [JUDGE_STATUS.TIME_LIMIT]: { abbr: 'TLE', label: '超出时间限制', color: COLOR_LIMIT_EXCEEDED },
  [JUDGE_STATUS.MEMORY_LIMIT]: { abbr: 'MLE', label: '超出内存限制', color: COLOR_LIMIT_EXCEEDED },
  [JUDGE_STATUS.RUNTIME_ERROR]: { abbr: 'RE', label: '运行错误', color: COLOR_RUNTIME_ERROR },
  [JUDGE_STATUS.COMPILE_ERROR]: { abbr: 'CE', label: '编译错误', color: COLOR_COMPILE_ERROR },
  [JUDGE_STATUS.SYSTEM_ERROR]: { abbr: 'UKE', label: '未知错误', color: '#4a90d9' },
  [JUDGE_STATUS.PENDING]: { abbr: 'PD', label: '等待', color: COLOR_NEUTRAL },
  [JUDGE_STATUS.RUNNING]: { abbr: 'RN', label: '评测中', color: COLOR_NEUTRAL },
}

// 评测机返回的状态名到结果编号，用于把文本状态归一成编号
const STATUS_NAME_TO_VALUE: Record<string, number> = {
  Accepted: JUDGE_STATUS.ACCEPTED,
  'Wrong Answer': JUDGE_STATUS.WRONG_ANSWER,
  'Time Limit Exceeded': JUDGE_STATUS.TIME_LIMIT,
  'Memory Limit Exceeded': JUDGE_STATUS.MEMORY_LIMIT,
  'Runtime Error': JUDGE_STATUS.RUNTIME_ERROR,
  'Nonzero Exit Status': JUDGE_STATUS.RUNTIME_ERROR,
  'Compile Error': JUDGE_STATUS.COMPILE_ERROR,
  'System Error': JUDGE_STATUS.SYSTEM_ERROR,
  'Internal Error': JUDGE_STATUS.SYSTEM_ERROR,
  'Invalid Request': JUDGE_STATUS.SYSTEM_ERROR,
}

// 状态名归一成结果编号，未收录时返回 null
export function statusValueOf(status: string | null | undefined): number | null {
  if (!status) return null
  return STATUS_NAME_TO_VALUE[status] ?? null
}

// 取状态样式，先按状态名查，再按是否通过兜底
export function judgeStatusStyle(
  status: string | null | undefined,
  passed?: boolean | null,
  value?: number | null,
): JudgeStatusStyle {
  const normalized = statusValueOf(status)
  if (normalized !== null) return STATUS_STYLES[normalized]!

  if (value !== undefined && value !== null && STATUS_STYLES[value]) {
    return STATUS_STYLES[value]!
  }

  return passed === true
    ? STATUS_STYLES[JUDGE_STATUS.ACCEPTED]!
    : STATUS_STYLES[JUDGE_STATUS.WRONG_ANSWER]!
}
