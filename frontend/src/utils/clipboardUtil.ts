// 剪贴板工具

// 在非安全上下文（如通过局域网 http 访问）里 navigator.clipboard 不可用，退回落文本域选中复制
function fallbackCopy(text: string): boolean {
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.setAttribute('readonly', '')
  textarea.style.position = 'fixed'
  textarea.style.top = '-9999px'
  document.body.appendChild(textarea)
  textarea.select()
  let copied = false
  try {
    copied = document.execCommand('copy')
  } catch {
    copied = false
  }
  document.body.removeChild(textarea)
  return copied
}

// 复制文本到剪贴板，返回是否成功；空文本直接视为失败
export async function copyText(text: string): Promise<boolean> {
  if (!text) return false
  if (navigator.clipboard?.writeText) {
    try {
      await navigator.clipboard.writeText(text)
      return true
    } catch {
      // 权限被拒或浏览器不支持时继续尝试兼容方案
    }
  }
  return fallbackCopy(text)
}
