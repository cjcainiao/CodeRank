<!-- 后台页面整体布局 -->
<template>
  <div
    class="backend-shell"
    :class="{
      'backend-shell--collapsed': isSidebarCollapsed,
      'backend-shell--dark': darkMode,
    }"
  >
    <BackendSidebar v-model:collapsed="isSidebarCollapsed" />

    <div class="backend-content">
      <BackendHistory
        v-model:collapsed="isSidebarCollapsed"
        :theme-mode="themeMode"
        @change-theme="changeTheme"
      />

      <main class="backend-main">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onUnmounted, provide, ref, watch } from 'vue'
import { useThemeMode } from '@/utils/themeModeUtil'
import BackendSidebar from './components/BackendSidebar.vue'
import BackendHistory from './components/BackendHistory.vue'

// 暗色模式挂在 html 上的类名，供传送门组件取用主题变量
const DARK_CLASS = 'backend-dark'

// 手机端默认收起侧栏
const isSidebarCollapsed = ref(window.matchMedia('(max-width: 760px)').matches)
const { themeMode, darkMode, changeTheme } = useThemeMode()
provide('backendDarkMode', darkMode)

// 暗色状态同步到 html，使挂到 body 的下拉、气泡也能读到主题变量
watch(
  darkMode,
  (dark) => {
    document.documentElement.classList.toggle(DARK_CLASS, dark)
  },
  { immediate: true },
)

onUnmounted(() => {
  document.documentElement.classList.remove(DARK_CLASS)
})

// 站点名称，供侧栏品牌与页面标题共用
const siteTitle = ref('CodeRank')
provide('backendSiteTitle', siteTitle)
</script>

<style scoped>
.backend-shell {
  color-scheme: light;
  display: grid;
  grid-template-areas:
    'brand content'
    'sidebar content';
  grid-template-columns: 260px minmax(0, 1fr);
  grid-template-rows: 44px minmax(0, 1fr);
  height: 100vh;
  height: 100dvh;
  overflow: hidden;
  background: var(--backend-surface);
  color: var(--backend-text);
  transition: grid-template-columns 320ms cubic-bezier(0.22, 1, 0.36, 1);
}

.backend-shell--dark {
  color-scheme: dark;
}

.backend-content {
  grid-area: content;
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  background: var(--backend-surface);
}

.backend-main {
  flex: 1;
  min-height: 0;
  padding: 30px;
  overflow-y: auto;
  background: var(--backend-surface);
}

.backend-shell--collapsed {
  grid-template-columns: 0 minmax(0, 1fr);
}

@media (max-width: 760px) {
  .backend-shell {
    display: block;
  }

  .backend-content {
    height: 100%;
  }

  .backend-main {
    padding: 22px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .backend-shell {
    transition: none;
  }
}
</style>
