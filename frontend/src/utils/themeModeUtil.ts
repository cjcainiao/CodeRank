// 颜色模式设置与系统配色同步
import { computed, ref } from 'vue'

// 可选的配色模式
export type ThemeMode = 'light' | 'dark' | 'system'

const THEME_MODE_STORAGE_KEY = 'coderank-theme-mode'
const SYSTEM_DARK_QUERY = '(prefers-color-scheme: dark)'

// 读取本地保存的模式，无效值按跟随系统处理
function readStoredMode(): ThemeMode {
  if (typeof window === 'undefined') return 'system'
  try {
    const raw = window.localStorage.getItem(THEME_MODE_STORAGE_KEY)
    if (raw === 'light' || raw === 'dark' || raw === 'system') return raw
  } catch {
    // 存储不可用时使用默认模式。
  }
  return 'system'
}

// 共享一份模式状态，多个组件读取同一份设置
const themeMode = ref<ThemeMode>(readStoredMode())
const systemDark = ref(false)
const darkMode = computed(
  () => themeMode.value === 'dark' || (themeMode.value === 'system' && systemDark.value),
)

// 监听系统配色变化，仅在跟随系统时生效
function syncSystemDark(event: MediaQueryList | MediaQueryListEvent): void {
  systemDark.value = event.matches
}

if (typeof window !== 'undefined') {
  const query = window.matchMedia(SYSTEM_DARK_QUERY)
  syncSystemDark(query)
  query.addEventListener('change', syncSystemDark)
}

// 颜色模式状态与切换入口
export function useThemeMode() {
  // 切换模式并写入本地存储
  function changeTheme(mode: ThemeMode): void {
    themeMode.value = mode
    if (typeof window === 'undefined') return
    try {
      window.localStorage.setItem(THEME_MODE_STORAGE_KEY, mode)
    } catch {
      // 写入失败时仍保留当前页面的选择。
    }
  }

  return { themeMode, darkMode, changeTheme }
}
