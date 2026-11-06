// 数字与序数文案工具

// 中文数字字符，用于把 0-9 拼成序数文案
const CHINESE_DIGITS = ['零', '一', '二', '三', '四', '五', '六', '七', '八', '九']

// 把正整数转成中文数字，如 1 → 一、12 → 十二、20 → 二十；超出两位数时退回阿拉伯数字
export function chineseNumeral(value: number): string {
  if (!Number.isInteger(value) || value <= 0) return String(value)
  if (value < 10) return CHINESE_DIGITS[value]!
  if (value < 20) return `十${value % 10 === 0 ? '' : CHINESE_DIGITS[value % 10]}`
  if (value < 100) {
    const ones = value % 10 === 0 ? '' : CHINESE_DIGITS[value % 10]
    return `${CHINESE_DIGITS[Math.floor(value / 10)]}十${ones}`
  }
  return String(value)
}
