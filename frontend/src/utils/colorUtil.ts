// 根据十六进制背景色选择对比度更高的文字颜色

// 自定义标签和色块在浅色背景上使用黑字，深色背景上使用白字
export function contrastTextColor(color: string): string {
  const hex = color.replace(/^#/, '')
  if (!/^(?:[0-9a-f]{3}|[0-9a-f]{6})$/i.test(hex)) return 'var(--backend-text)'

  const expanded = hex.length === 3 ? [...hex].map((digit) => digit + digit).join('') : hex
  const channels = [0, 2, 4].map((index) => {
    const channel = Number.parseInt(expanded.slice(index, index + 2), 16) / 255
    return channel <= 0.04045 ? channel / 12.92 : ((channel + 0.055) / 1.055) ** 2.4
  })
  const luminance = channels[0]! * 0.2126 + channels[1]! * 0.7152 + channels[2]! * 0.0722

  return luminance > 0.179 ? '#000' : '#fff'
}
